package com.zsq.winter.design.strategy;

/**
 * 统一的枚举抽象接口，所有用于策略选择的枚举类型应实现该接口。
 *
 * <p>设计目的：
 * <ul>
 *   <li>为策略工厂提供稳定的、可序列化的数值标识（{@link #getCode()}）。</li>
 *   <li>为运维/日志/展示提供人类可读的文本说明（{@link #getDesc()}）。</li>
 * </ul>
 * </p>
 *
 * <p>约定与建议：
 * <ul>
 *   <li>code 应在全局范围内保持唯一与稳定，适合用于持久化或与外部系统交互。</li>
 *   <li>desc 仅作为说明文案，不参与业务逻辑判断。</li>
 * </ul>
 * </p>
 *
 * <p>典型示例：
 * <pre>{@code
 * public enum PaymentMethod implements BaseEnum {
 *     ALIPAY(1, "支付宝"),
 *     WECHAT(2, "微信支付");
 *
 *     private final int code;
 *     private final String desc;
 *
 *     PaymentMethod(int code, String desc) {
 *         this.code = code;
 *         this.desc = desc;
 *     }
 *
 *     @Override
 *     public int getCode() { return code; }
 *
 *     @Override
 *     public String getDesc() { return desc; }
 * }
 * }
 * </pre>
 * </p>
 */
public interface BaseEnum {
    /**
     * 返回唯一且稳定的数值标识，用于策略映射与持久化。
     * @return 唯一的数值标识 code
     */
    String getCode();

    /**
     * 返回人类可读的描述文本，不应参与业务逻辑判断。
     * @return 描述文本
     */
    String getDesc();
}