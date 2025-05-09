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

## 打包 & 运行

```shell
mvn clean package -DskipTests

java -Dfile.encoding=UTF-8 -jar condlk-user-1.0.0.jar --server.ssl.key-store=classpath:ssl_prod/keystore.p12
```

## 自建 CA + 受信任证书

✅ SpringBoot 默认支持.p12 的SSL 证书，此后可通过https://192.168.0.20:9081/ 访问接口。

✅ 安装 .p12 或 .cer 到系统，意味着：

Windows / macOS 系统可以识别证书；

但 Chrome/Edge/Safari 等浏览器需要你把根证书添加到“受信任的根证书颁发机构”（Trusted Root CAs）。

如果证书不是由公认的机构（如 Let’s Encrypt、DigiCert 等）签发，而是自签名的或你自己本地搭的 CA，浏览器一律视为 不安全。

因此通过下面的脚本自建CA + 受信任证书：

```shell
#!/bin/bash
set -e

# 📌 根据实际情况调整【配置参数】
IP=192.168.0.20
DAYS=365
PASSWORD=changeit
OUTPUT_ZIP=certs_$IP.zip

echo "📌 生成根 CA"
openssl genrsa -out rootCA.key 2048
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days $DAYS -out rootCA.pem -subj "/CN=Local Dev Root CA"

# 生成 .cer（供浏览器导入）
openssl x509 -outform der -in rootCA.pem -out rootCA.cer
echo "✅ 已生成 rootCA.cer（请双击导入受信任的根证书）"

echo "📌 生成服务器私钥和证书请求 (CSR)"
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr -subj "/CN=$IP"

echo "📌 创建证书扩展配置 v3.ext"
cat > v3.ext <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names

[alt_names]
IP.1 = $IP
EOF

echo "📌 签发服务器证书"
openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial \
  -out server.crt -days $DAYS -sha256 -extfile v3.ext

echo "✅ 已生成 server.crt 和 server.key"

echo "📌 创建 PKCS12 (.p12) 文件"
openssl pkcs12 -export \
  -in server.crt \
  -inkey server.key \
  -certfile rootCA.pem \
  -out keystore.p12 \
  -name springboot \
  -password pass:$PASSWORD

echo "✅ 已生成 keystore.p12（密码：$PASSWORD）"

echo "📦 打包所有生成的证书到 $OUTPUT_ZIP"
zip -j $OUTPUT_ZIP rootCA.* server.* keystore.p12 v3.ext > /dev/null

echo "🎉 所有证书文件已打包为：$OUTPUT_ZIP"

echo sudo chown ubuntu:ubuntu $OUTPUT_ZIP
```

### 📦 输出文件说明

| 文件             | 说明                                       |
|----------------|------------------------------------------|
| `rootCA.pem`   | 根证书（PEM）                                 |
| `rootCA.cer`   | Windows 安装导入此证书（DER）✅浏览器信任关键             |
| `server.crt`   | 服务器证书（Spring Boot 使用）                    |
| `server.key`   | 私钥                                       |
| `keystore.p12` | Spring Boot 配置 `server.ssl.key-store` 使用 |
| `v3.ext`       | 添加 SAN（Subject Alternative Name）配置       |

## 启动脚本

```
@echo off

chcp 65001 >nul
setlocal enabledelayedexpansion

:: 定义服务路径
set ES_PATH=D:\java\elasticsearch-9.0.0\bin
set REDIS_PATH=D:\java\Redis-7.2.4-Windows-x64-msys2
set JAR_PATH=D:\java\condlk-user-1.0.0.jar

:: 启动 MySQL
echo 启动 MySQL...
net start mysql >nul 2>&1
echo MySQL 启动成功！

:: 启动 Elasticsearch
echo 启动 Elasticsearch...
start "" /b cmd /c "cd /d %ES_PATH% && elasticsearch.bat"
echo 等待 Elasticsearch 启动（检测端口 9200）...

:wait_for_es
timeout /t 2 >nul
netstat -ano | findstr ":9200" >nul
if errorlevel 1 (
    echo 正在等待 Elasticsearch...
    goto wait_for_es
)
echo Elasticsearch 启动成功！

:: 启动 Redis
echo 启动 Redis...
start "" /b cmd /c "cd /d %REDIS_PATH% && redis-server.exe redis.conf"
echo Redis 启动成功！

:: 启动 Java 服务并保存 PID
echo 启动 Java 服务...
cd /d D:\java
start "" /b cmd /c "java -Dfile.encoding=UTF-8 -jar %JAR_PATH%  --server.ssl.key-store=classpath:ssl_prod/keystore.p12"
:: 获取 Java PID（通过端口、可执行名也可实现更精确）
for /f "tokens=2" %%a in ('tasklist /fi "imagename eq java.exe" /fo table ^| findstr /i java.exe') do (
    set JAVA_PID=%%a
)

:: 注册 Ctrl+C 中断处理
echo.
echo 服务已全部启动，按 Ctrl+C 终止所有服务...
echo.

:: 进入挂起状态，保持前台窗口运行
:wait
timeout /t 9999 >nul
goto wait

:: Ctrl+C 处理
:on_break
echo.
echo 检测到 Ctrl+C，正在关闭所有服务...

echo 终止 Java 服务...
taskkill /f /pid !JAVA_PID! >nul 2>&1

echo 终止 Elasticsearch...
taskkill /f /im "java.exe" >nul 2>&1

echo 终止 Redis...
taskkill /f /im "redis-server.exe" >nul 2>&1

echo 所有服务已关闭。
exit /b

```