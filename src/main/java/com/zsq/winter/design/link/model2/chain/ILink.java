package com.zsq.winter.design.link.model2.chain;

/**
 * 简化版链表操作接口，定义基础的增删查与打印能力。
 *
 * @param <E> 链表中存放的元素类型
 */
public interface ILink<E> {

    /**
     * 在链表尾部添加元素。
     * @param e 元素
     * @return 添加是否成功
     */
    boolean add(E e);

    /**
     * 在链表头部添加元素。
     * @param e 元素
     * @return 添加是否成功
     */
    boolean addFirst(E e);

    /**
     * 在链表尾部添加元素（与 {@link #add(Object)} 等效）。
     * @param e 元素
     * @return 添加是否成功
     */
    boolean addLast(E e);

    /**
     * 移除指定的元素（按第一次匹配移除）。
     * @param o 目标元素
     * @return 是否成功移除
     */
    boolean remove(Object o);

    /**
     * 按索引获取元素。
     * @param index 索引（0 基）
     * @return 对应位置的元素
     */
    E get(int index);

    /**
     * 打印链表的整体结构与元素。
     */
    void printLinkList();

}
