package com.zsq.winter.design.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 带多线程预处理的策略路由。
 * 在 Java 中：
 * 抽象类可以不实现它所继承（或实现）的接口中的所有方法，可以实现一部分吗，都不实现也可以，剩下的交给子类去实现即可。
 * 所以继承目前这个抽象类的话，子类必须实现get()方法，抽象方法multiThread()和doApply()方法。父类已经实现了apply方法，这个apply方法会调用子类对应实现的multiThread()和doApply()方法
 * 子类调用该父类的router方法，该方法会调用子类实现的get()（用来指定下一个节点）和该父类的apply()方法，父类的apply()方法会调用子类实现的doApply()方法进行业务处理，通常需要进行调用router方法
 * 也就是说子类的get()方法需要判断和执行下一个节点的任务
 * <p>在执行主策略前，先通过多线程异步加载数据或预计算，随后进行主业务受理。</p>
 */
public abstract class AbstractMultiThreadStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

    /**
     * 默认策略处理器，当无法映射到具体策略时执行。
     */
    @Getter
    @Setter
    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    /**
     * 根据映射器获取策略并进行路由执行。
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 策略执行结果；若无策略映射则返回默认策略的结果
     * @throws Exception 获取或执行策略过程中可能的异常
     */
    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if (null != strategyHandler) return strategyHandler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

    /**
     * 先进行多线程数据准备，再执行业务受理。
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 受理结果
     * @throws Exception 受理过程中可能的异常
     */
    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        // 异步加载数据
        multiThread(requestParameter, dynamicContext);
        // 业务流程受理
        return doApply(requestParameter, dynamicContext);
    }

    /**
     * 功能：异步加载数据或进行预计算。
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @throws ExecutionException   任务执行异常
     * @throws InterruptedException 线程中断异常
     * @throws TimeoutException     任务超时异常
     */
    protected abstract void multiThread(T requestParameter, D dynamicContext) throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * 功能：主业务受理逻辑，用来判断下一个节点的流向
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 处理结果
     * @throws Exception 处理过程中可能的异常
     */
    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception;

}
