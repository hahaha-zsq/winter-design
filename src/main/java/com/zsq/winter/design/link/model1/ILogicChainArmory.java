package com.zsq.winter.design.link.model1;


/**
 * 逻辑链“军械库”接口，用于维护链式调用中的“下一个”节点。
 *
 * <p>在链式职责模式中，每个节点需要能够指向下一个节点，
 * 并支持在构建链时进行追加，从而形成可迭代的处理链。</p>
 *
 * <p>
 * 泛型参数说明：
 * <ul>
 *   <li>T：请求参数类型（每个链节点处理的输入数据）</li>
 *   <li>D：动态上下文类型（在链路中传递的可变上下文，例如运行时状态、临时数据等）</li>
 *   <li>R：处理结果类型（链节点产出的结果，可能在链中传递或最终返回）</li>
 * </ul>
 * </p>
 */
public interface ILogicChainArmory<T, D, R> {

    /**
     * 获取当前链节点的“下一个”节点。
     *
     * @return 下一个逻辑链节点，如果尚未设置则可能为 {@code null}
     */
    ILogicLink<T, D, R> next();

    /**
     * 追加并设置当前节点的“下一个”节点。
     *
     * <p>通常在构建链路时调用，用于将新节点接入链表尾部。</p>
     *
     * @param next 要追加的下一个节点
     * @return 传入的 {@code next} 本身，便于链式调用
     */
    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);

}
