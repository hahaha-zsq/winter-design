package com.zsq.winter.design.strategy;

/**
 * 通用策略接口，配合 {@link AbstractStrategyFactory} 使用以实现可插拔的策略模式。
 *
 * <p>该接口通过类型参数 {@code T} 绑定策略的枚举类型（需实现 {@link BaseEnum}），
 * 工厂将以 {@link BaseEnum#getCode()} 作为唯一键完成策略的注册与查找。</p>
 *
 * <p>使用建议：
 * <ul>
 *   <li>为避免 {@code Object...} 带来的类型不安全，建议定义明确的入参/出参模型，
 *       并在调用时仅传递一个类型良好的请求对象，例如 <code>PaymentRequest</code>。</li>
 *   <li>策略实现应返回自身绑定的枚举类型，以便工厂进行唯一注册。</li>
 * </ul>
 * </p>
 *
 * <p>示例：
 * <pre>{@code
 * public class AlipayPayStrategy implements PaymentStrategy {
 *     @Override
 *     public void execute(Object... params) {
 *         PaymentRequest req = (PaymentRequest) params[0];
 *         // 处理支付逻辑...
 *         System.out.println("支付宝支付：" + req.getOrderId());
 *     }
 *
 *     @Override
 *     public PaymentMethod getStrategyType() {
 *         return PaymentMethod.ALIPAY;
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @param <T> 策略所绑定的枚举类型，需实现 {@link BaseEnum}
 */
public interface BaseStrategy<T extends Enum<T> & BaseEnum> {

    /**
     * 策略的主要执行逻辑。
     *
     * <p>调用约定：通常传入一个领域请求对象（例如 {@code PaymentRequest}），
     * 实现方应进行必要的类型检查与转换。</p>
     * 这是默认的通用逻辑，使用的是Object类型的数据，对于需要精确程度的如金钱相关的，可以定义一个接口继承它，并提供相对应需要高精度参数类型的参数就行
     * 对应的策略实现类就需要实现这两个方法，策略工厂调用具体方法时，选择pay方法即可
     * <pre>{@code
     * public interface PayStrategy extends BaseStrategy<PayType> {
     *     void pay(double amount);
     *
     *     @Override
     *     default void execute(Object... params) {
     *         pay((double) params[0]);
     *     }
     * }
     *
     * }</pre>
     *
     * @param params 可变参数，建议仅传递一个强类型的请求对象
     */
    void execute(Object... params);

    /**
     * 返回该策略对应的枚举类型，用于唯一注册与查找。
     * @return 策略绑定的枚举类型
     */
    T getStrategyType();
}