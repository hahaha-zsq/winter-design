package com.zsq.winter.design.link.model1;


/**
 * 逻辑链节点接口，代表链式职责中的一个具体“处理单元”。
 *
 * <p>每个节点应实现具体的业务处理逻辑，并可通过继承的军械库接口维护链路结构。</p>
 */
public interface ILogicLink<T, D, R> extends ILogicChainArmory<T, D, R> {

    /**
     * 执行业务处理逻辑。
     *
     * <p>实现类可在方法内根据需要决定是否继续传递到下一个节点，
     * 或者在当前节点直接返回结果。</p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext  动态上下文（在链中共享与传递的状态）
     * @return 处理结果
     * @throws Exception 处理过程中可能抛出的异常（例如校验失败、外部服务错误等）
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

}
