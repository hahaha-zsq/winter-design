# Winter Design DDC Spring Boot Starter

> 为业务应用提供通用、抽象且可扩展的设计模式模板。本项目示范了“策略模式”的标准抽象与在支付域的落地实现，支持 Java SPI 自动发现与运行期手动注册，面向开源项目的阅读与扩展体验进行了优化。

## 目录
- [设计思想](#设计思想)
- [核心抽象（策略模式）](#核心抽象策略模式)
- [模型设计图](#模型设计图)
- [示例模块与整体实现图](#示例模块与整体实现图)
- [快速开始](#快速开始)
- [扩展新的策略](#扩展新的策略)
- [注意事项](#注意事项)
- [License](#license)

## 设计思想
- 用领域枚举绑定策略，枚举提供稳定的 `code` 与人类可读的 `desc`；工厂以 `code → strategy` 映射管理策略实现。
- 策略接口统一约定 `execute(Object...)` 与 `getStrategyType()`；建议将实际入参封装为强类型领域对象（如 `PaymentRequest`），提升类型安全与可维护性。
- 工厂支持两种来源：
  - Java SPI 自动发现：在 `META-INF/services/<策略接口全名>` 中声明实现类，构造工厂时自动加载。
  - 运行期手动注册：构造或调用时追加策略，实现更灵活的扩展场景。
- 通过 `EnumUtils.getByCode` 从整型 `code` 安全映射到枚举实例，适合持久化、对外协议或前端传参场景。

## 核心抽象（策略模式）
```java
// BaseEnum：统一的枚举抽象接口
public interface BaseEnum {
    int getCode();
    String getDesc();
}

// BaseStrategy：通用策略接口，绑定枚举类型 T
public interface BaseStrategy<T extends Enum<T> & BaseEnum> {
    void execute(Object... params);
    T getStrategyType();
}

// EnumUtils：按 code 查找枚举实例
public final class EnumUtils {
    public static <E extends Enum<E> & BaseEnum> E getByCode(Class<E> enumClass, int code) { /*...*/ }
}

// AbstractStrategyFactory：策略工厂（SPI + 手动注册）
public abstract class AbstractStrategyFactory<T extends Enum<T> & BaseEnum, S extends BaseStrategy<T>> {
    public void registerStrategy(S strategy) { /*...*/ }
    public void registerStrategies(Collection<S> strategies) { /*...*/ }
    public S getStrategy(T type) { /*...*/ }
    public S getStrategy(int code, Class<T> enumClass) { /*...*/ }
    public Map<Integer, S> getAllStrategies() { /*...*/ }
}
```

### 为什么这么做，带来哪些好处
- 解耦与内聚：业务枚举明确策略类型边界，策略实现仅关注自身逻辑；工厂负责生命周期与查找，职责清晰。
- 可插拔扩展：SPI 自动发现让新增策略“零改造”；手动注册适配运行期动态扩展或非 SPI 场景。
- 统一映射与稳定标识：以枚举 `code` 为唯一键，便于持久化与跨系统交互，同时保留 `desc` 作为展示文案。
- 类型安全的调用约定：建议用强类型请求对象承载入参，避免 `Object...` 的不安全使用。
- 简化使用：通过枚举或 `code` 即可获取策略，支持多种接入方式（枚举、整型、SPI）。

## 模型设计图
策略抽象的架构图（strategy 文件夹核心类关系）：

```mermaid
classDiagram
    class BaseEnum {
        <<interface>>
        +getCode() int
        +getDesc() String
    }
    
    class BaseStrategy~T~ {
        <<interface>>
        +execute(Object... params) void
        +getStrategyType() T
    }
    
    class EnumUtils {
        <<utility>>
        +getByCode(Class~E~ enumClass, int code)$ E
    }
    
    class AbstractStrategyFactory~T, S~ {
        <<abstract>>
        -Map~Integer, S~ strategyMap
        -Class~S~ strategyClass
        +AbstractStrategyFactory(Class~S~ strategyClass)
        +AbstractStrategyFactory(Class~S~ strategyClass, Collection~S~ strategies)
        +registerStrategy(S strategy) void
        +registerStrategies(Collection~S~ strategies) void
        +getStrategy(T type) S
        +getStrategy(int code, Class~T~ enumClass) S
        +getAllStrategies() Map~Integer, S~
        -loadBySpi() void
    }
    
    BaseStrategy ..> BaseEnum : 绑定枚举类型 T
    AbstractStrategyFactory ..> BaseEnum : 使用 T extends BaseEnum
    AbstractStrategyFactory ..> BaseStrategy : 管理 S extends BaseStrategy
    AbstractStrategyFactory ..> EnumUtils : 使用 code 查找枚举
    
    note for BaseEnum "提供稳定的 code 与描述\n适合持久化与跨系统交互"
    note for BaseStrategy "统一策略执行约定\n建议使用强类型请求对象"
    note for AbstractStrategyFactory "支持 SPI 自动加载\n与手动注册两种方式"
```

## 示例模块与整体实现图
### 完整代码示例

#### 1. 支付方式枚举（PaymentMethod.java）

```java
package com.zsq.winter.examples.payment;

import com.zsq.winter.design.strategy.BaseEnum;

/**
 * 支付方式枚举，实现 BaseEnum 以提供稳定的 code 与描述。
 */
public enum PaymentMethod implements BaseEnum {
    ALIPAY(1, "支付宝"),
    WECHAT(2, "微信支付"),
    CREDIT_CARD(3, "信用卡");

    private final int code;
    private final String desc;

    PaymentMethod(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() { 
        return code; 
    }

    @Override
    public String getDesc() { 
        return desc; 
    }
}
```

#### 2. 策略接口（PaymentStrategy.java）

```java
package com.zsq.winter.examples.payment;

import com.zsq.winter.design.strategy.BaseStrategy;

/**
 * 支付策略接口，绑定到 {@link PaymentMethod} 枚举。
 */
public interface PaymentStrategy extends BaseStrategy<PaymentMethod> {
}
```

#### 3. 策略工厂（PaymentStrategyFactory.java）

```java
package com.zsq.winter.examples.payment;

import com.zsq.winter.design.strategy.AbstractStrategyFactory;

import java.util.List;

/**
 * 支付策略工厂，支持 SPI 自动加载与手动追加策略。
 */
public class PaymentStrategyFactory extends AbstractStrategyFactory<PaymentMethod, PaymentStrategy> {

    /**
     * 仅通过 SPI 加载策略实现。
     */
    public PaymentStrategyFactory() {
        super(PaymentStrategy.class);
    }

    /**
     * 通过 SPI 加载并追加手动传入的策略实现。
     *
     * @param extraStrategies 需要额外注册的策略列表
     */
    public PaymentStrategyFactory(List<PaymentStrategy> extraStrategies) {
        super(PaymentStrategy.class, extraStrategies);
    }
}
```

#### 4. 请求模型（PaymentRequest.java）

```java
package com.zsq.winter.examples.payment;

/**
 * 简单的支付请求模型，用于作为策略执行的入参。
 */
public class PaymentRequest {
    private final String orderId;
    private final long amountCents;

    public PaymentRequest(String orderId, long amountCents) {
        this.orderId = orderId;
        this.amountCents = amountCents;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "orderId='" + orderId + '\'' +
                ", amountCents=" + amountCents +
                '}';
    }
}
```

#### 5. 支付宝策略实现（AlipayPaymentStrategy.java）

```java
package com.zsq.winter.examples.payment;

/**
 * 支付宝支付策略实现。
 */
public class AlipayPaymentStrategy implements PaymentStrategy {

    @Override
    public void execute(Object... params) {
        PaymentRequest req = (PaymentRequest) params[0];
        System.out.println("[Alipay] 支付订单:" + req.getOrderId() + ", 金额:" + req.getAmountCents());
    }

    @Override
    public PaymentMethod getStrategyType() {
        return PaymentMethod.ALIPAY;
    }
}
```

#### 6. 微信支付策略实现（WechatPaymentStrategy.java）

```java
package com.zsq.winter.examples.payment;

/**
 * 微信支付策略实现。
 */
public class WechatPaymentStrategy implements PaymentStrategy {

    @Override
    public void execute(Object... params) {
        PaymentRequest req = (PaymentRequest) params[0];
        System.out.println("[Wechat] 支付订单:" + req.getOrderId() + ", 金额:" + req.getAmountCents());
    }

    @Override
    public PaymentMethod getStrategyType() {
        return PaymentMethod.WECHAT;
    }
}
```

#### 7. 信用卡支付策略实现（CreditCardPaymentStrategy.java）

```java
package com.zsq.winter.examples.payment;

/**
 * 信用卡支付策略实现（示例中通过手动注册）。
 */
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public void execute(Object... params) {
        PaymentRequest req = (PaymentRequest) params[0];
        System.out.println("[CreditCard] 支付订单:" + req.getOrderId() + ", 金额:" + req.getAmountCents());
    }

    @Override
    public PaymentMethod getStrategyType() {
        return PaymentMethod.CREDIT_CARD;
    }
}
```

#### 8. 使用示例（PaymentDemo.java）

```java
package com.zsq.winter.examples.payment;

import java.util.Arrays;

/**
 * 支付策略使用示例：演示 SPI 加载与手动追加策略的组合使用。
 */
public class PaymentDemo {
    public static void main(String[] args) {
        // 示例 1：仅使用 SPI 加载（Alipay、Wechat）
        PaymentStrategyFactory spiFactory = new PaymentStrategyFactory();
        PaymentRequest req1 = new PaymentRequest("order001", 1999);
        PaymentStrategy wechat = spiFactory.getStrategy(PaymentMethod.WECHAT);
        if (wechat != null) {
            wechat.execute(req1);
        }

        // 通过 code 获取策略（等同于上面的枚举方式）
        PaymentStrategy alipay = spiFactory.getStrategy(1, PaymentMethod.class);
        if (alipay != null) {
            alipay.execute(new PaymentRequest("order002", 2999));
        }

        // 示例 2：在 SPI 基础上手动追加（CreditCard）
        PaymentStrategyFactory mixedFactory = new PaymentStrategyFactory(
                Arrays.asList(new CreditCardPaymentStrategy())
        );
        PaymentStrategy card = mixedFactory.getStrategy(PaymentMethod.CREDIT_CARD);
        if (card != null) {
            card.execute(new PaymentRequest("order003", 8888));
        }
    }
}
```

#### 9. SPI 配置文件

在 `src/main/resources/META-INF/services/` 目录下创建文件：

文件名：`com.zsq.winter.examples.payment.PaymentStrategy`

文件内容：
```
com.zsq.winter.examples.payment.AlipayPaymentStrategy
com.zsq.winter.examples.payment.WechatPaymentStrategy
```

#### 10. 运行输出

```
[Wechat] 支付订单:order001, 金额:1999
[Alipay] 支付订单:order002, 金额:2999
[CreditCard] 支付订单:order003, 金额:8888
```

### 支付域类关系图

```mermaid
classDiagram
    %% 核心抽象层（strategy 文件夹）
    class BaseEnum {
        <<interface>>
        +getCode() int
        +getDesc() String
    }
    
    class BaseStrategy~T~ {
        <<interface>>
        +execute(Object... params) void
        +getStrategyType() T
    }
    
    class AbstractStrategyFactory~T, S~ {
        <<abstract>>
        +getStrategy(T type) S
        +getStrategy(int code, Class~T~ enumClass) S
        +registerStrategy(S strategy) void
    }
    
    %% 支付域实现层（examples.payment 文件夹）
    class PaymentMethod {
        <<enumeration>>
        ALIPAY(1, "支付宝")
        WECHAT(2, "微信支付")
        CREDIT_CARD(3, "信用卡")
        -int code
        -String desc
        +getCode() int
        +getDesc() String
    }
    
    class PaymentStrategy {
        <<interface>>
    }
    
    class PaymentStrategyFactory {
        +PaymentStrategyFactory()
        +PaymentStrategyFactory(List~PaymentStrategy~ extras)
    }
    
    class AlipayPaymentStrategy {
        +execute(Object... params) void
        +getStrategyType() PaymentMethod
    }
    
    class WechatPaymentStrategy {
        +execute(Object... params) void
        +getStrategyType() PaymentMethod
    }
    
    class CreditCardPaymentStrategy {
        +execute(Object... params) void
        +getStrategyType() PaymentMethod
    }
    
    class PaymentRequest {
        -String orderId
        -long amountCents
        +getOrderId() String
        +getAmountCents() long
    }
    
    class PaymentDemo {
        +main(String[] args)$ void
    }
    
    %% 继承与实现关系
    PaymentMethod ..|> BaseEnum : 实现
    PaymentStrategy --|> BaseStrategy : 继承
    PaymentStrategyFactory --|> AbstractStrategyFactory : 继承
    
    AlipayPaymentStrategy ..|> PaymentStrategy : 实现
    WechatPaymentStrategy ..|> PaymentStrategy : 实现
    CreditCardPaymentStrategy ..|> PaymentStrategy : 实现
    
    %% 依赖关系
    PaymentStrategy ..> PaymentMethod : 绑定枚举
    PaymentStrategyFactory ..> PaymentStrategy : 管理
    PaymentStrategyFactory ..> PaymentMethod : 使用
    
    AlipayPaymentStrategy ..> PaymentRequest : 使用
    WechatPaymentStrategy ..> PaymentRequest : 使用
    CreditCardPaymentStrategy ..> PaymentRequest : 使用
    
    PaymentDemo ..> PaymentStrategyFactory : 创建工厂
    PaymentDemo ..> PaymentStrategy : 获取策略
    PaymentDemo ..> PaymentRequest : 创建请求
    
    note for PaymentMethod "领域枚举：定义支付方式\ncode 用于持久化与映射"
    note for PaymentStrategyFactory "工厂：SPI 加载 Alipay/Wechat\n手动注册 CreditCard"
    note for PaymentRequest "强类型请求对象\n提升类型安全"
```

### 策略执行流程图

```mermaid
sequenceDiagram
    autonumber
    participant Demo as 💻 PaymentDemo
    participant Factory as 🏭 PaymentStrategyFactory
    participant SPI as 🔌 ServiceLoader(SPI)
    participant Strategy as 🎯 PaymentStrategy
    participant Request as 📦 PaymentRequest
    
    Note over Demo,Factory: 🔵 场景1：SPI 自动加载
    Demo->>+Factory: new PaymentStrategyFactory()
    Factory->>+SPI: ServiceLoader.load(PaymentStrategy.class)
    SPI-->>-Factory: AlipayPaymentStrategy, WechatPaymentStrategy
    Factory->>Factory: registerStrategy(each)
    Factory-->>-Demo: 工厂实例
    
    Demo->>+Factory: getStrategy(PaymentMethod.WECHAT)
    Factory-->>-Demo: WechatPaymentStrategy
    Demo->>+Request: new PaymentRequest("order001", 1999)
    Request-->>-Demo: 请求对象
    Demo->>+Strategy: execute(request)
    Strategy->>Strategy: 处理微信支付逻辑
    Strategy-->>-Demo: 执行完成
    
    
    Note over Demo,Factory: 🟢 场景2：通过 code 获取策略
    Demo->>+Factory: getStrategy(1, PaymentMethod.class)
    Factory->>Factory: EnumUtils.getByCode(PaymentMethod, 1)
    Note right of Factory: code=1 → ALIPAY
    Factory->>Factory: strategyMap.get(ALIPAY.code)
    Factory-->>-Demo: AlipayPaymentStrategy
    Demo->>+Request: new PaymentRequest("order002", 2999)
    Request-->>-Demo: 请求对象
    Demo->>+Strategy: execute(request)
    Strategy->>Strategy: 处理支付宝支付逻辑
    Strategy-->>-Demo: 执行完成
    
    Note over Demo,Factory: 🟠 场景3：SPI + 手动注册
    Demo->>+Factory: new PaymentStrategyFactory(List.of(CreditCardStrategy))
    Factory->>+SPI: ServiceLoader.load(PaymentStrategy.class)
    SPI-->>-Factory: AlipayPaymentStrategy, WechatPaymentStrategy
    Note right of Factory: SPI 加载完成
    Factory->>Factory: registerStrategies(extras)
    Note right of Factory: 手动注册 CreditCard
    Factory-->>-Demo: 工厂实例
    
    Demo->>+Factory: getStrategy(PaymentMethod.CREDIT_CARD)
    Factory-->>-Demo: CreditCardPaymentStrategy
    Demo->>+Request: new PaymentRequest("order003", 8888)
    Request-->>-Demo: 请求对象
    Demo->>+Strategy: execute(request)
    Strategy->>Strategy: 处理信用卡支付逻辑
    Strategy-->>-Demo: 执行完成
```

### 策略注册与查找流程图

```mermaid
flowchart TD
    Start([开始使用策略模式]) --> Choice{选择加载方式}
    
    Choice -->|SPI 自动加载| SPI[🔌 配置 SPI 文件]
    Choice -->|手动注册| Manual[✋ 准备策略实例列表]
    Choice -->|混合模式| Both[🔄 SPI + 手动]
    
    SPI --> SPIFile[📄 META-INF/services/<br/>PaymentStrategy]
    SPIFile --> SPIContent[声明实现类:<br/>AlipayPaymentStrategy<br/>WechatPaymentStrategy]
    SPIContent --> CreateFactory1[new PaymentStrategyFactory]
    
    Manual --> ManualList[List.of<br/>CreditCardPaymentStrategy]
    ManualList --> CreateFactory2[new PaymentStrategyFactory<br/>extras]
    
    Both --> BothConfig[SPI 文件 + 额外列表]
    BothConfig --> CreateFactory3[new PaymentStrategyFactory<br/>extras]
    
    CreateFactory1 --> FactoryInit{工厂初始化}
    CreateFactory2 --> FactoryInit
    CreateFactory3 --> FactoryInit
    
    FactoryInit -->|1| LoadSPI[🔌 loadBySpi<br/>ServiceLoader.load]
    LoadSPI --> RegisterSPI[📝 registerStrategy<br/>for each SPI strategy]
    
    FactoryInit -->|2| LoadManual[✋ registerStrategies<br/>extras]
    
    RegisterSPI --> StrategyMap[(🗺️ strategyMap<br/>code → strategy)]
    LoadManual --> StrategyMap
    
    StrategyMap --> Ready[✅ 工厂就绪]
    
    Ready --> GetChoice{获取策略方式}
    
    GetChoice -->|枚举| GetByEnum[getStrategy<br/>PaymentMethod.WECHAT]
    GetChoice -->|code| GetByCode[getStrategy<br/>1, PaymentMethod.class]
    
    GetByEnum --> DirectGet[strategyMap.get<br/>type.getCode]
    GetByCode --> EnumConvert[EnumUtils.getByCode<br/>code → enum]
    EnumConvert --> DirectGet
    
    DirectGet --> StrategyInstance[🎯 策略实例]
    
    StrategyInstance --> CreateRequest[📦 new PaymentRequest<br/>orderId, amount]
    CreateRequest --> Execute[⚡ strategy.execute<br/>request]
    
    Execute --> Business[💼 执行业务逻辑]
    Business --> End([完成])
    
    style Start fill:#e1f5ff
    style Choice fill:#fff4e1
    style SPI fill:#e8f5e9
    style Manual fill:#fff3e0
    style Both fill:#f3e5f5
    style FactoryInit fill:#ffebee
    style StrategyMap fill:#e0f2f1
    style Ready fill:#c8e6c9
    style GetChoice fill:#fff9c4
    style StrategyInstance fill:#b2dfdb
    style Execute fill:#ffccbc
    style Business fill:#d1c4e9
    style End fill:#e1f5ff
```

### 工厂内部注册机制详解

```mermaid
flowchart LR
    subgraph Input[📥 输入来源]
        direction TB
        SPI[🔌 SPI 自动发现<br/>ServiceLoader]
        Manual[✋ 手动传入<br/>Collection]
    end
    
    subgraph Factory[🏭 AbstractStrategyFactory]
        direction TB
        Register[registerStrategy]
        Validate{验证策略}
        CheckDup{检查重复 code?}
        PutMap[putIfAbsent<br/>strategyMap]
        Error[❌ 抛出异常<br/>IllegalStateException]
    end
    
    subgraph Storage[💾 存储结构]
        direction TB
        Map[(Map&lt;Integer, S&gt;<br/>strategyMap)]
        Key[Key: type.getCode]
        Value[Value: strategy 实例]
    end
    
    subgraph Output[📤 对外接口]
        direction TB
        Get1[getStrategy<br/>T type]
        Get2[getStrategy<br/>int code, Class]
        GetAll[getAllStrategies]
    end
    
    SPI --> Register
    Manual --> Register
    
    Register --> Validate
    Validate -->|strategy != null| CheckDup
    Validate -->|null| Error
    
    CheckDup -->|无重复| PutMap
    CheckDup -->|已存在| Error
    
    PutMap --> Map
    Map --> Key
    Map --> Value
    
    Map --> Get1
    Map --> Get2
    Map --> GetAll
    
    Get1 --> Result[🎯 策略实例]
    Get2 --> EnumUtil[EnumUtils.getByCode]
    EnumUtil --> Get1
    GetAll --> AllResult[🗺️ 所有策略]
    
    style Input fill:#e3f2fd
    style Register fill:#ffccbc
    style Map fill:#c8e6c9
    style Result fill:#b2dfdb
    style Error fill:#ffcdd2
```


## 扩展新的策略
- 新增枚举项并确保 `code` 唯一与稳定（适合持久化与跨系统交互）。
- 新增策略实现并返回该枚举项；入参尽量使用强类型请求对象以确保类型安全。
- 选择 SPI 或手动注册：
  - SPI：在 `META-INF/services/<策略接口全名>` 添加实现类全名，工厂自动发现。
  - 手动：在构造工厂或运行时调用 `registerStrategies(...)` 追加。

## 注意事项
- 并发与生命周期：默认实现未做并发保护，建议在应用启动阶段完成策略注册（实现 InitializingBean 或使用 @PostConstruct）；如需运行期动态变更，请在外层加同步控制。

## License
本项目基于 Apache-2.0 许可证发布，详情参见 [pom.xml 中的声明](../pom.xml)。