package com.zsq.winter.design.strategy;

import java.util.*;

/**
 * 通用策略工厂，支持通过 Java SPI 与手动注册两种方式组合加载策略实现。
 *
 * <p>核心能力：
 * <ul>
 *   <li>根据策略枚举的唯一 {@code code} 注册并查找具体策略实现。</li>
 *   <li>通过 {@link ServiceLoader} 自动发现实现类（需配置 {@code META-INF/services}）。</li>
 *   <li>支持在运行期追加手动注册，实现灵活扩展。</li>
 * </ul>
 * </p>
 *
 * <p>类型参数说明：
 * <ul>
 *   <li>{@code T}：策略所绑定的枚举类型，需实现 {@link BaseEnum}，用于提供唯一 {@code code}。</li>
 *   <li>{@code S}：策略接口类型，需实现 {@link BaseStrategy} 并返回其绑定的枚举类型。</li>
 * </ul>
 * </p>
 *
 * <p>SPI 使用说明：
 * <ul>
 *   <li>构造工厂时会尝试使用 {@link ServiceLoader#load(Class)} 发现并注册所有实现。</li>
 *   <li>需在资源目录创建文件：{@code META-INF/services/完全限定的策略接口名}，内容为实现类的完全限定名（每行一个）。</li>
 *   <li>若发现重复 {@code code}，将抛出 {@link IllegalStateException}，不会覆盖已有注册。</li>
 * </ul>
 * </p>
 *
 * <p>线程安全：
 * <br>默认实现未做并发保护，建议在应用启动阶段完成策略注册；如需在运行期动态变更，可在外层加同步控制。</p>
 *
 * <p>支付场景示例（SPI + 手动一起使用）：
 * <pre>{@code
 * // 1) 定义策略接口与枚举（示例位于 com.zsq.winter.examples.payment 包）
 * public interface PaymentStrategy extends BaseStrategy<PaymentMethod> {}
 * public enum PaymentMethod implements BaseEnum { ALIPAY(1,"支付宝"), WECHAT(2,"微信") // ... }
 *
 * // 2) 具体策略实现（SPI 文件 META-INF/services/com.zsq.winter.examples.payment.PaymentStrategy 中声明）
 * public class AlipayPaymentStrategy implements PaymentStrategy { // getStrategyType->ALIPAY, execute(...) }
 * public class WechatPaymentStrategy implements PaymentStrategy { // getStrategyType->WECHAT, execute(...) }
 *
 * // 3) 定义工厂
 * public class PaymentStrategyFactory extends AbstractStrategyFactory<PaymentMethod, PaymentStrategy> {
 *     public PaymentStrategyFactory() { super(PaymentStrategy.class); } // 仅 SPI
 *     public PaymentStrategyFactory(List<PaymentStrategy> extras) { super(PaymentStrategy.class, extras); } // SPI+手动
 * }
 *
 * // 4) 组合使用
 * PaymentStrategyFactory factory1 = new PaymentStrategyFactory(); // 加载 SPI 中的支付宝、微信
 * PaymentStrategy wechat = factory1.getStrategy(PaymentMethod.WECHAT);
 * wechat.execute(new PaymentRequest("order001", 1999));
 *
 * // 手动追加一个未在 SPI 中声明的策略（例如 CreditCard）
 * PaymentStrategyFactory factory2 = new PaymentStrategyFactory(Arrays.asList(new CreditCardPaymentStrategy()));
 * PaymentStrategy card = factory2.getStrategy(PaymentMethod.CREDIT_CARD);
 * card.execute(new PaymentRequest("order002", 8888));
 * }
 * </pre>
 * </p>
 *
 * @param <T> 策略绑定的枚举类型，需实现 {@link BaseEnum}
 * @param <S> 策略接口类型，需实现 {@link BaseStrategy}
 */
public abstract class AbstractStrategyFactory<
        T extends Enum<T> & BaseEnum,
        S extends BaseStrategy<T>> {

    private final Map<Integer, S> strategyMap = new LinkedHashMap<>();
    private final Class<S> strategyClass;

    /**
     * 构造函数：仅通过 SPI 加载策略实现。
     *
     * @param strategyClass 策略接口的 {@link Class}，用于 SPI 发现
     */
    protected AbstractStrategyFactory(Class<S> strategyClass) {
        this(strategyClass, Collections.emptyList());
    }

    /**
     * 构造函数：通过 SPI 加载 + 手动注册额外策略。
     *
     * <p>注意：当手动注册的策略与 SPI 已加载的策略出现相同 {@code code} 时，会抛出异常，
     * 不会覆盖前者。</p>
     *
     * @param strategyClass 策略接口的 {@link Class}
     * @param extraStrategies 需要额外注册的策略列表，可为空
     */
    protected AbstractStrategyFactory(Class<S> strategyClass, List<S> extraStrategies) {
        this.strategyClass = strategyClass;
        loadBySpi();
        registerStrategies(extraStrategies);
    }

    /**
     * 注册单个策略实现。
     *
     * @param strategy 策略实现实例，需返回非空的枚举类型
     * @throws IllegalStateException 当存在重复 {@code code} 时抛出
     */
    public void registerStrategy(S strategy) {
        Objects.requireNonNull(strategy, "strategy must not be null");
        T type = Objects.requireNonNull(strategy.getStrategyType(), "strategyType must not be null");
        int code = type.getCode();
        S previous = strategyMap.putIfAbsent(code, strategy);
        if (previous != null) {
            throw new IllegalStateException("重复注册策略，code=" + code);
        }
    }

    /**
     * 批量注册策略实现。
     *
     * @param strategies 策略集合，为空时忽略
     */
    public void registerStrategies(Collection<S> strategies) {
        if (strategies == null) return;
        for (S s : strategies) {
            registerStrategy(s);
        }
    }

    /**
     * 通过枚举类型获取策略实现。
     *
     * @param type 策略枚举类型
     * @return 匹配的策略实现，若未注册则返回 {@code null}
     */
    public S getStrategy(T type) {
        return strategyMap.get(type.getCode());
    }

    /**
     * 通过整型 {@code code} 获取策略实现。
     *
     * @param code 枚举的唯一数值标识
     * @param enumClass 枚举的 {@link Class}
     * @return 匹配的策略实现，若未注册则返回 {@code null}
     * @throws IllegalArgumentException 当 {@code code} 不属于该枚举类型时抛出
     */
    public S getStrategy(int code, Class<T> enumClass) {
        T type = EnumUtils.getByCode(enumClass, code);
        return strategyMap.get(type.getCode());
    }

    /**
     * 查看所有已注册策略的只读映射。
     *
     * @return {@code code -> strategy} 的不可变映射
     */
    public Map<Integer, S> getAllStrategies() {
        return Collections.unmodifiableMap(strategyMap);
    }

    /**
     * 通过 SPI 加载并注册策略实现。
     */
    private void loadBySpi() {
        try {
            ServiceLoader<S> loader = ServiceLoader.load(strategyClass);
            for (S strategy : loader) {
                registerStrategy(strategy);
            }
        } catch (Exception e) {
            System.err.println("SPI 加载策略失败: " + e.getMessage());
        }
    }
}