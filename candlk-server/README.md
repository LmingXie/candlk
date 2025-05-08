# Candlk

项目服务 公共依赖的父 POM

- Spring Cloud 2021.0.x
- Spring Cloud Alibaba 2021.0.x

## JDK 17 add-opens

```shell
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
--add-opens=java.base/java.math=ALL-UNNAMED
# 下面的一般用不到
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/java.nio=ALL-UNNAMED
--add-opens=java.base/java.security=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.base/java.time=ALL-UNNAMED
--add-opens=java.base/jdk.internal.access=ALL-UNNAMED
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED
--add-opens=java.sql/java.sql=ALL-UNNAMED
```

## HanLP 分词器

下载 HanLP 最新模型数据包（data-for-1.7.5.zip）：https://github.com/hankcs/HanLP/releases

解压缩后修改[hanlp.properties](condlk-user%2Fsrc%2Fmain%2Fresources%2Fhanlp.properties)中的`root``跟路径（使用绝对路径！）

## Windows ElasticSearch 9.0

### 调整JVM内存配置

在.\elasticsearch-9.0.0\config\jvm.options.d下创建``jvm.options``文件，内容如下（根据实际情况调整）：

```angular2html
-Xms2g
-Xmx2g
```

### 调整``elasticsearch.bat``启动脚本JDK配置

```shell
set JAVA_HOME=D:\Program Files\elasticsearch-9.0.0\jdk
```

### ES 插件安装

```shell
# IK分词器（参考：https://release.infinilabs.com/analysis-ik/stable/）
elasticsearch-plugin install https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-9.0.0.zip

# 日语分词器
elasticsearch-plugin install analysis-kuromoji
# 韩语分词器
elasticsearch-plugin install analysis-nori
```

## 打包
```shell
mvn clean package -DskipTests

java -jar target/condlk-user-1.0.0.jar --spring.profiles.active=prod
```

## 启动脚本
```
@echo off
chcp 65001 >nul

echo 启动 MySQL...
net start mysql
echo MySQL 启动成功！

echo 启动 Elasticsearch...
start "" /b cmd /c "cd /d D:\java\elasticsearch-9.0.0\bin && elasticsearch.bat"
echo ES 服务启动成功！

echo 启动 Redis...
start "" /b cmd /c "cd /d D:\java\Redis-7.2.4-Windows-x64-msys2 && redis-server.exe redis.conf"
echo Redis 启动成功！

echo 等待 20 秒以确保依赖服务启动...
timeout /t 20 /nobreak >nul

echo 启动 Java 服务...
cd /d D:\java
java -jar condlk-user-1.0.0.jar --spring.profiles.active=prod

exit

```