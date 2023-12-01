# common-redis

基于 Spring Data Redis 和 Lettuce 相关的基础设施 starter 抽象封装库。

### 配置参考
```yaml
spring:
  redis:
    host: localhost
    port: 6379 # 默认=6379
    password: password # 默认=
    database: 0 # 默认=0
    timeout: 10000 # 10s
    lettuce:
      pool:
        max-idle: 8 # 默认=8
        min-idle: 1 # 默认=0
        max-active: 8 # 默认=8
        max-wait: 30000 # 30s
redison:
  subscriptionConnectionPoolSize: 50  # 订阅连接池大小 
  subscriptionConnectionMinimumIdleSize: 1  # 订阅连接最小空闲大小 
  connectionMinimumIdleSize: 4   # 连接最小空闲大小 
  connectionPoolSize: 16  # 连接池大小 
```
