package com.zsq.winter.design.link.model2.chain;


import com.zsq.winter.design.link.model2.DynamicContext;
import com.zsq.winter.design.link.model2.handler.ILogicHandler;

/**
 * 面向业务的链表结构，节点类型为 {@link ILogicHandler}，并可作为一个整体参与链式处理。
 *
 * <p>该类既是链表（用于存放处理器），又是一个处理器（实现 ILogicHandler），
 * 因此可以将整个链表作为单个节点嵌入更复杂的处理结构中。</p>
 */
public class BusinessLinkedList<T, D extends DynamicContext, R> extends LinkedList<ILogicHandler<T, D, R>> implements ILogicHandler<T, D, R>{

    /**
     * 使用链路名称进行构造。
     *
     * @param name 链表名称，用于标识与日志等
     */
    public BusinessLinkedList(String name) {
        super(name);
    }

    /**
     * 依次执行链表中的每个处理器，直到链表结束或上下文要求停止。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>从头节点开始遍历</li>
     *   <li>调用当前处理器的 {@code apply} 方法</li>
     *   <li>若 {@code dynamicContext.isProceed()} 为 {@code false}，则立即返回该处理器产生的结果</li>
     *   <li>否则继续遍历下一个节点，直到遍历完成</li>
     * </ol>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return 若中途停止，返回停止时的结果；若全部处理完且未中断，返回 {@code null}
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        Node<ILogicHandler<T, D, R>> current = this.first;
        do {
            ILogicHandler<T, D, R> item = current.item;
            R apply = item.apply(requestParameter, dynamicContext);
            // 上下文控制：若要求停止继续处理，则返回当前结果
            if (!dynamicContext.isProceed()) return apply;

            current = current.next;
        } while (null != current);

        // 全部节点执行完成且未中断，返回空结果（根据业务自行约定）
        return null;
    }

}
