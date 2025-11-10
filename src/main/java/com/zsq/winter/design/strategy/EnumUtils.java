package com.zsq.winter.design.strategy;

/**
 * 枚举工具类，提供基于 {@link BaseEnum#getCode()} 的枚举查找能力。
 *
 * <p>该工具简化了从整型 code 到具体枚举实例的映射逻辑，
 * 适合与 {@link AbstractStrategyFactory} 的按 code 获取策略功能配合使用。</p>
 */
public final class EnumUtils {
    private EnumUtils() {}

    /**
     * 根据唯一的整型 {@code code} 查找枚举实例。
     * <E extends Enum<E> & BaseEnum> 泛型 E 不仅要是枚举类型，还必须实现接口 BaseEnum
     * @param enumClass 目标枚举类型，需实现 {@link BaseEnum}
     * @param code 唯一且稳定的枚举数值标识
     * @param <E> 枚举泛型参数
     * @return 与 {@code code} 对应的枚举实例
     * @throws IllegalArgumentException 当未找到匹配的枚举项时抛出
     */
    public static <E extends Enum<E> & BaseEnum> E getByCode(Class<E> enumClass, int code) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知类型 code: " + code + " in " + enumClass.getSimpleName());
    }
}