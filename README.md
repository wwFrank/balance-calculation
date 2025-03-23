## 一、代码结构
### architecture.png 架构图
![architecture.png 架构图](https://raw.githubusercontent.com/wwFrank/balance-calculation/refs/heads/main/architecture.png)
重要！重要！重要！
系统架构视频讲解：https://v.youku.com/video?vid=XNjQ3MDc4MTgyMA%3D%3D

### calculation SpringBoot工程
```txt
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hsbc
│   │   │           └── calculation
│   │   │               ├── config
│   │   │               │   └── RedisConfig.java ## redis配置
│   │   │               ├── constants
│   │   │               │   └── TransactionConstants.java ## 常量
│   │   │               ├── controller
│   │   │               │   └── TransactionController.java ## 交易核心Controller
│   │   │               │   └── MockGeneratorController.java ## Mock生成指定数量测试数据
│   │   │               ├── domain
│   │   │               │   ├── TransactionDO.java ## 交易对象
│   │   │               │   └── UserAccountDO.java ## 用户账户对象
│   │   │               ├── interceptor
│   │   │               │   └── RateLimitingInterceptor.java ## 限流配拦截器
│   │   │               ├── limiting
│   │   │               │   └── RateLimitingInterceptor.java ## 限流配置
│   │   │               ├── repository
│   │   │               │   └── TransactionRepository.java  ## 交易数据库Jpa实现
│   │   │               │   └── UserAccountRepository.java  ## 用户账号数据Jpa实现
│   │   │               ├── result
│   │   │               │   └── TransactionResult.java
│   │   │               ├── service
│   │   │               │   └── RandomDataService.java ## 生成Mock数据服务
│   │   │               │   └── TransactionService.java ## 交易操作Service 核心业务逻辑在这里
│   │   │               │   └── UserRedisService.java ## 用户账户缓存操作
│   │   │               └── BalanceServiceApplication.java
│   │   └── resources
│   │       ├── application.properties
│   │       └── data.sql ## 建表sql
│   └── test
│       └── java
│   │   │   └── com
│   │   │       └── hsbc
│   │   │           └── calculation
│   │   │               ├── utils
│   │   │               │   └── ConvertUtil.java
│   │   │               ├── TransactionUnitTest.java ## 单元测试
│   │   │               └── TransactionIntegrationTest.java ## 集成测试
└── pom.xml
```

## 二、架构图
参考工程代码architecture.png图片

## 三、功能介绍
本系统用SpringBoot及其插件实现了一个处理金融交易并实时更新账户余额的服务，在服务中通过一下具体操作实现服务的数据一致性、高可用和扩展性：
- 1、确保数据一致性和完整性，并且使用到数据库事务（spring-tx）
- 2、为失败的交易实现重试机制（spring-retry）
- 3、使用分布式缓存服务（spring-data-redis)
- 4、优化服务以处理高频交易（Google guava的RateLimiting限流保护）
- 5、使用JUnit编写单元测试（TransactionUnitTest）
- 6、编写集成测试，以确保服务与数据库和缓存正确配合（TransactionIntegrationTest）
- 7、用负载测试工具（例如Apache JMeter） 来测量性能
- 8、使用模拟数据生成器来模拟大量交易和账户余额进行测试（javafaker创建mock数据）
- 9、进行弹性测试，确保服务能从故障中恢复（阿里云ECS服务器重启演练）
- 10、服务DB是接入的Aliyun MySQL，数据量达到千万之后，可以通过分库分表和读写分离实现扩容
- 11、服务cache接入的是Aliyun Redis分布式缓存，可以实现自主水平扩容

## 四、测试报告：
### 单元测试报告-TransactionUnitTest
```txt
-------------------------------------------------------------------------------
Test set: com.hsbc.calculation.TransactionUnitTest
-------------------------------------------------------------------------------
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.338 s - in com.hsbc.calculation.TransactionUnitTest
```
### 集成测试报告-TransactionIntegrationTest
```txt
-------------------------------------------------------------------------------
Test set: com.hsbc.calculation.TransactionIntegrationTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.197 s - in com.hsbc.calculation.TransactionIntegrationTest
```
