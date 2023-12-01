# common-dao   

基于 MyBatis-Plus 的数据访问层基础设施 starter 封装库，默认同时开启 **声明式事务** 和 **注解式事务**。

声明式事务规则如下：
```
get*    =   readOnly
list*   =   readOnly
page*   =   readOnly
find*   =   readOnly
*       =   required
```

### 参考配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql:///dbName?useUnicode=true
    username: root
    hikari:
      data-source-properties:
        characterEncoding: UTF-8
        serverTimezone: GMT+8
        useSSL: false # 5.5.45+ / 5.6.26+ / 5.7.6+ 默认为 true
        allowPublicKeyRetrieval: true
        useLocalSessionState: true
        useUnbufferedInput: false
#        maintainTimeStats: false #为false减少系统时间调用
        elideSetAutoCommits: true
        cacheServerConfiguration: true
#        enableQueryTimeouts: false # 高性能负载环境下可以考虑为false
#        connectionAttributes: none # since 5.1.25(MySQL 5.6+) 可提高数据库连接创建/初始化速度
        useServerPrepStmts: true
        cachePrepStmts: true
        prepStmtCacheSize: 500 # 默认=25
        prepStmtCacheSqlLimit: 1024 # 默认=256

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      table-prefix: dt_
      id-type: auto
      logic-delete-field: state
      logic-delete-value: 0
      logic-not-delete-value: 3
  type-aliases-package: com.candlk.**.bean
  type-enums-package: com.candlk.**.bean
```
