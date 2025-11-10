package com.zsq.winter.design.link.model2.chain;


/**
 * 精简版双向链表实现，用于承载业务处理器等元素。
 *
 * <p>提供基本的插入、删除、查询以及打印操作，
 * 并维护头尾节点与大小等状态。</p>
 */
public class LinkedList<E> implements ILink<E> {

    /**
     * 责任链名称
     */
    private final String name;

    /**
     * 当前链表元素数量。
     */
    transient int size = 0;

    /**
     * 头节点引用。
     */
    transient Node<E> first;

    /**
     * 尾节点引用。
     */
    transient Node<E> last;

    /**
     * 使用链表名称进行构造。
     * @param name 链表名称
     */
    public LinkedList(String name) {
        this.name = name;
    }

    /**
     * 将元素插入到头部。
     *
     * @param e 元素
     */
    void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
    }

    /**
     * 将元素插入到尾部。
     *
     * @param e 元素
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
    }

    /**
     * 在尾部添加元素。
     */
    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 在头部添加元素。
     */
    @Override
    public boolean addFirst(E e) {
        linkFirst(e);
        return true;
    }

    /**
     * 在尾部添加元素。
     */
    @Override
    public boolean addLast(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 移除链表中的指定元素（匹配到第一个即移除）。
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将目标节点从链表中断开并维护相邻引用。
     */
    E unlink(Node<E> x) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        return element;
    }

    /**
     * 通过索引获取元素。
     */
    @Override
    public E get(int index) {
        return node(index).item;
    }

    /**
     * 通过索引定位节点，前半段从头遍历，后半段从尾遍历以提升效率。
     */
    Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    /**
     * 打印链表的头尾节点与整体元素顺序。
     */
    public void printLinkList() {
        if (this.size == 0) {
            System.out.println("链表为空");
        } else {
            Node<E> temp = first;
            System.out.print("目前的列表，头节点：" + first.item + " 尾节点：" + last.item + " 整体：");
            while (temp != null) {
                System.out.print(temp.item + "，");
                temp = temp.next;
            }
            System.out.println();
        }
    }

    /**
     * 链表节点对象，维护元素与前后指针。
     */
    protected static class Node<E> {

        /** 元素值 */
        E item;
        /** 后继节点 */
        Node<E> next;
        /** 前驱节点 */
        Node<E> prev;

        /**
         * 构造一个节点。
         * @param prev    前驱节点
         * @param element 元素值
         * @param next    后继节点
         */
        public Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }

    }

    /**
     * 获取链表名称。
     *
     * @return 链表名称
     */
    public String getName() {
        return name;
    }

}
