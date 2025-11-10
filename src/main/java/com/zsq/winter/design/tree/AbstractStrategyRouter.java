package com.zsq.winter.design.tree;

import lombok.Getter;
import lombok.Setter;

/**
 * 策略路由抽象基类：根据入参与上下文选择具体策略并执行。
 * 在 Java 中：
 * 抽象类可以不实现它所继承（或实现）的接口中的所有方法，可以实现一部分吗，都不实现也可以，剩下的交给子类去实现即可。
 * 所以继承目前这个抽象类的话，子类必须实现get()和apply()方法
 * 子类调用该父类的router方法，该方法会调用子类实现的get()和apply()方法，也就是说子类的get()和apply()方法需要判断和执行下一个节点的任务
 *
 * <p>通过 {@link StrategyMapper#get(Object, Object)} 获取策略处理器；
 * 若映射不到有效策略，则回退到 {@link #defaultStrategyHandler}。</p>
 */
public abstract class AbstractStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

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
        if(null != strategyHandler) return strategyHandler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

}
