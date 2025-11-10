package com.zsq.winter.design.link.model2.handler;


import com.zsq.winter.design.link.model2.DynamicContext;

/**
 * 业务逻辑处理器接口，定义链式节点的基本行为。
 *
 * <p>处理器通常在 {@link #apply(Object, DynamicContext)} 中执行业务，
 * 并可通过 {@link #next(Object, DynamicContext)} 或 {@link #stop(Object, DynamicContext, Object)}
 * 控制链路向后是否继续。</p>
 */
public interface ILogicHandler<T, D extends DynamicContext, R> {

    /**
     * 标记允许继续向后执行，并返回 {@code null}（表示不干预链路的返回值）。
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return {@code null}
     */
    default R next(T requestParameter, D dynamicContext) {
        dynamicContext.setProceed(true);
        return null;
    }

    /**
     * 标记停止向后执行，并返回给定的结果。
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @param result           希望停止时返回的结果
     * @return 传入的结果
     */
    default R stop(T requestParameter, D dynamicContext, R result){
        dynamicContext.setProceed(false);
        return result;
    }

    /**
     * 执行业务处理逻辑。
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return 处理结果（可为 {@code null}），若需中断链路可配合 {@link #stop(Object, DynamicContext, Object)}
     * @throws Exception 处理过程中可能抛出的异常
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}
