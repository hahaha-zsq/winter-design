package com.zsq.winter.design.tree;

/**
 * 策略映射器：根据入参与上下文确定应执行的策略处理器。
 *   T 入参类型
 *   D 上下文参数
 *   R 返参类型
 */
public interface StrategyMapper<T, D, R> {

    /**
     * 获取待执行策略。
     *
     * @param requestParameter 入参（用于决定策略）
     * @param dynamicContext   上下文（可辅助判断策略）
     * @return 对应的策略处理器；若无法确定可返回 {@code null}
     * @throws Exception 映射过程中可能的异常
     */
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;

}
