package com.zsq.winter.design.tree;

/**
 * 策略处理器接口，封装具体策略的执行逻辑。
 * T 入参类型
 * D 上下文参数
 * R 返参类型
 * <p>可与 {@link StrategyMapper} 配合使用，根据路由选择相应策略进行执行。</p>
 */
public interface StrategyHandler<T, D, R> {

    /**
     * 默认策略处理器：不做任何处理并返回 {@code null}。
     */
    StrategyHandler DEFAULT = (T, D) -> null;

    /**
     * 执行策略逻辑。
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 执行结果（可能为 {@code null}）
     * @throws Exception 执行过程中可能的异常
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}