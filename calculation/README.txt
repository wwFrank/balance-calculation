一、代码结构
architecture.png ## 架构图
calculation ## SpringBoot工程
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
│   │   │               └── BalanceServiceApplication.java ## 集成测试
└── pom.xml



测试报告：
单元测试报告-TransactionUnitTest
-------------------------------------------------------------------------------
Test set: com.hsbc.calculation.TransactionUnitTest
-------------------------------------------------------------------------------
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.338 s - in com.hsbc.calculation.TransactionUnitTest
集成测试报告-TransactionIntegrationTest
-------------------------------------------------------------------------------
Test set: com.hsbc.calculation.TransactionIntegrationTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.197 s - in com.hsbc.calculation.TransactionIntegrationTest