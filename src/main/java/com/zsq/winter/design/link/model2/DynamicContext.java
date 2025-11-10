package com.zsq.winter.design.link.model2;

import java.util.HashMap;
import java.util.Map;

/**
 * 链式处理的动态上下文对象。
 *
 * <p>用于在链路节点之间传递临时数据与状态，例如中间计算结果、控制是否继续向后处理等。</p>
 */
public class DynamicContext {

    /**
     * 控制链路是否继续向后执行。
     * <p>默认 {@code true}，表示继续传递；当业务需要中断链路时可设置为 {@code false}。</p>
     */
    private boolean proceed;

    /**
     * 构造方法，默认允许继续执行。
     */
    public DynamicContext() {
        this.proceed = true;
    }

    /**
     * 存放在链路中共享的动态数据。
     * <p>通过 {@link #setValue(String, Object)} 与 {@link #getValue(String)} 读写。</p>
     */
    private Map<String, Object> dataObjects = new HashMap<>();

    /**
     * 设置上下文中的数据。
     *
     * @param key   键，建议使用常量或枚举保证一致性
     * @param value 值，任意类型
     * @param <T>   值的泛型类型
     */
    public <T> void setValue(String key, T value) {
        dataObjects.put(key, value);
    }

    /**
     * 获取上下文中的数据。
     *
     * @param key 键
     * @param <T> 期望返回的类型
     * @return 存储的值；若不存在则返回 {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) dataObjects.get(key);
    }

    /**
     * 是否继续向后执行链路。
     *
     * @return {@code true} 继续执行；{@code false} 中断执行
     */
    public boolean isProceed() {
        return proceed;
    }

    /**
     * 设置是否继续向后执行链路。
     *
     * @param proceed {@code true} 继续执行；{@code false} 中断链路
     */
    public void setProceed(boolean proceed) {
        this.proceed = proceed;
    }
}
