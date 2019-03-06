# bucket test

## Introduction
- bucket test提供bts（bucket test ）功能，支持按某种维度做分流测试。
- 常见的场景
    - 按用户（userId）分流5%的流量测试新功能
    - 按访问浏览器（浏览器uuid）分流10%的流量测试新系统

### 分流测试示意
![分流测试示意](http://aligitlab.oss-cn-hangzhou-zmf.aliyuncs.com/uploads/alibaba-common/smart-bts/642fe2d35d407ac2c9c4b00adcc071b0/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7_2016-08-18_%E4%B8%8B%E5%8D%883.38.31.png)

```
graph TB
    A((用户流量)) ==>|默认流量| B((原有功能))
    A((用户流量)) ==>|分流测试bts| C((新增功能))
```

### 分流测试逻辑
![分流测试逻辑](http://aligitlab.oss-cn-hangzhou-zmf.aliyuncs.com/uploads/alibaba-common/smart-bts/aa52cd0421ab5873c7ad7a871624d208/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7_2016-08-18_%E4%B8%8B%E5%8D%883.38.43.png)

```
graph TB
    A[用户访问] -->|分桶| B(分桶计算)
    B --> C{是否分流}
    C -->|Yes| D[新增功能]
    C -->|No| E[原有功能]
```

## Quick Start

### 1. 引入依赖
    
```xml
<!-- required:smart-bts依赖 -->
<dependency>
     <groupId>org.springframework.ext</groupId>
     <artifactId>bucket-test</artifactId>
     <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置bean
```xml
<!-- 静态注入 -->
<bean id="bucketTestHelper" class="com.springframework.ext.common.bts.BucketTestHelper" init-method="init">
    <property name="cacheClient" ref="guavaCacheClient"/>
</bean>
<!-- 内存cache -->
<bean id="guavaCacheClient" class="org.springframework.ext.common.cache.GuavaCacheClient" init-method="init"/>
```

### 3. 分桶配置
- 在配置中心上申请一个配置，例如

```json
[{"name":"smartDesign", "percent":1, "basic":100, "status":1, "hits":["3665061551","3700502224","3665051611"], "excludes":["3665061555","3700502223","3665051688"]}]
```

### 4. 使用示例

```java
// 计算是否在分桶中
boolean isBucket = BucketTestHelper.instance("[{\"name\":\"smartDedign\", \"percent\":1, \"basic\":100, \"status\":1, \"hits\":[\"3665061551\",\"3700502224\",\"3665051611\"], \"excludes\":[\"3665061555\",\"3700502223\",\"3665051688\"]}]").isBucket("smartDesign", userId);
// 设置标志位到context
context.put("showSmartDesign", isBucket);
```

## Features

### 1. 名词解释
- 分流测试：将流量总值视为100%，切入一定比例流量到需要测试的功能，称之为分流测试
- 分流比例：需要测试的流量比例
- 分桶：可以将流量均分为N部分，每一部分为一个分桶；分桶从0号桶开始，直至（N-1）号桶
- 白名单：维度（用户id、浏览器uuid等）白名单，白名单中的流量，判断结果一直为isBucket=true
- 黑名单：维度（用户id、浏览器uuid等）黑名单，黑名单中的流量，判断结果一直为isBucket=false

### 2. 分桶配置
- name:分桶测试标识，唯一索引
- percent:分流比例分子，默认0（不分流），分流比例为percent/basic
- basic:分流比例分母，默认100，分流比例为percent/basic
- status:是否有效状态，0 未启用; 1 启用中; 2 已废弃 
- hits:白名单，命中则直接返回0号分桶；白名单优先匹配
- excludes:黑名单，永远不命中
