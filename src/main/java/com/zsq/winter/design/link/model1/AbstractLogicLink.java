package com.zsq.winter.design.link.model1;


/**
 * 逻辑链抽象基类，提供通用的“链路维护”与“向后传递”能力。
 *
 * <p>继承此类的具体节点只需专注于自身的处理逻辑，
 * 链路的拼装与传递由该抽象类统一处理。</p>
 */
public abstract class AbstractLogicLink<T, D, R> implements ILogicLink<T, D, R> {

    /**
     * 当前节点指向的下一个逻辑节点。
     */
    private ILogicLink<T, D, R> next;

    /**
     * 返回下一个逻辑节点。
     *
     * @return 下一个逻辑链节点，可能为 {@code null}
     */
    @Override
    public ILogicLink<T, D, R> next() {
        return next;
    }

    /**
     * 追加并设置下一个逻辑节点。
     *
     * @param next 要设置的下一节点
     * @return 传入的 {@code next} 本身，便于链式拼装
     */
    @Override
    public ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next) {
        this.next = next;
        return next;
    }

    /**
     * 将请求传递到下一个节点并返回其处理结果。
     *
     * <p>通常在当前节点完成本地处理后调用，
     * 若不存在下一个节点（{@code next} 为 {@code null}），
     * 需要在具体实现中自行判空或在构建链路时保证完整性。</p>
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return 下一个节点的处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    protected R next(T requestParameter, D dynamicContext) throws Exception {
        return next.apply(requestParameter, dynamicContext);
    }

}
