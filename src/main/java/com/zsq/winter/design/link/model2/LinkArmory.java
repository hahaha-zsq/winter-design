package com.zsq.winter.design.link.model2;


import com.zsq.winter.design.link.model2.chain.BusinessLinkedList;
import com.zsq.winter.design.link.model2.handler.ILogicHandler;

/**
 * 链路“军械库”构造器，负责将一组业务处理器拼装为可执行的链表结构。
 *
 * <p>通过传入链路名称与多个处理器，按顺序构建 {@link BusinessLinkedList}，
 * 以支持链式职责的执行。</p>
 */
public class LinkArmory<T, D extends DynamicContext, R> {

    /**
     * 业务逻辑链表，保存按顺序拼装的处理器节点。
     */
    private final BusinessLinkedList<T, D, R> logicLink;

    /**
     * 构造并拼装逻辑链表。
     *
     * @param linkName      链路名称，用于标识与日志打印等场景
     * @param logicHandlers 业务处理器列表，按参数顺序加入链表
     */
    @SafeVarargs
    public LinkArmory(String linkName, ILogicHandler<T, D, R>... logicHandlers) {
        logicLink = new BusinessLinkedList<>(linkName);
        for (ILogicHandler<T, D, R> logicHandler: logicHandlers){
            logicLink.add(logicHandler);
        }
    }

    /**
     * 获取拼装完成的逻辑链表。
     *
     * @return 业务逻辑链表实例
     */
    public BusinessLinkedList<T, D, R> getLogicLink() {
        return logicLink;
    }

}
