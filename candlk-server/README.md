# Candlk

项目服务 公共依赖的父 POM

- Spring Cloud 2021.0.x
- Spring Cloud Alibaba 2021.0.x

### JDK 17 add-opens
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

#### 常用 Redis 命令

```shell
DEL playLogSyncRelay
DEL userPlayInOuts
DEL upgrade:user
DEL userPlayIns-*
DEL merchantStageTotalWins
DEL merchantTypeWins
```

### 启动命令
```shell
#!/bin/bash

# --- 配置区 ---
APP_DIR="/mnt/bet"
UPLOAD_DIR="/home/ubuntu"
JAR_NAME="condlk-user-1.0.0.jar"
BAK_NAME="condlk-user-1.0.0.jar.bak"
LOG_FILE="$APP_DIR/logs/webapp.log"
PORT=8126

# 合并后的 JVM 参数
JVM_OPTS="-Djdk.internal.httpclient.disableHostnameVerification=true \
-Djdk.http.auth.proxying.disabledSchemes= \
-Djdk.http.auth.tunneling.disabledSchemes= \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
-Xms1024m \
-Xmx4096m \
-XX:MaxMetaspaceSize=768m"

# 切换到工作目录
cd $APP_DIR || exit 1

# 确保日志目录存在
mkdir -p "$APP_DIR/logs"

# --- 函数定义：优雅停机 ---
stop_service() {
    echo "正在检查端口 $PORT..."
    PID=$(lsof -t -i:$PORT)
    if [ -n "$PID" ]; then
        echo "发现服务正在运行 (PID: $PID)，正在发送停止信号..."
        kill -15 $PID
        for i in {1..10}; do
            if ! lsof -i:$PORT > /dev/null; then
                echo "服务已成功停止。"
                return 0
            fi
            echo "等待服务释放端口... ($i/10)"
            sleep 2
        done
        echo "警告：服务未能优雅停止，尝试强制杀死..."
        kill -9 $PID
    else
        echo "端口 $PORT 未被占用，无需停止。"
    fi
}

# --- 逻辑处理 ---

# 1. 停止模式 (-stop)
if [ "$1" == "-stop" ]; then
    echo "执行停止指令..."
    stop_service
    exit 0
fi

# 2. 回滚模式 (-r)
if [ "$1" == "-r" ]; then
    echo "执行恢复模式 (-r)..."
    stop_service
    if [ -f "$BAK_NAME" ]; then
        echo "回滚文件: $BAK_NAME -> $JAR_NAME"
        mv "$BAK_NAME" "$JAR_NAME"
    else
        echo "错误：未找到备份文件 $BAK_NAME，无法回滚。"
        exit 1
    fi
fi

# 3. 正常升级流程 (无参数且非回滚时)
if [ -z "$1" ]; then
    stop_service
    if [ -f "$UPLOAD_DIR/$JAR_NAME" ]; then
        echo "检测到新安装包，开始升级..."
        if [ -f "$JAR_NAME" ]; then
            mv "$JAR_NAME" "$BAK_NAME"
            echo "已备份旧包。"
        fi
        mv "$UPLOAD_DIR/$JAR_NAME" "$APP_DIR/" || { echo "移动失败！"; exit 1; }
        echo "新包已就绪。"
    else
        echo "未发现新安装包，直接重启现有程序。"
    fi
fi

# 4. 启动服务逻辑
if [ -f "$JAR_NAME" ]; then
    echo "正在后台启动 $JAR_NAME..."
    
    SPRING_CONFIG="--spring.config.location=file:$APP_DIR/application.yml"
    
    # 丢弃标准输出，但保留应用内部通过 logback/log4j 写入 webapp.log 的能力
    nohup java $JVM_OPTS \
        -jar $JAR_NAME \
        $SPRING_CONFIG \
        > /dev/null 2>&1 &

    echo "启动指令已发出，正在等待日志输出..."
    sleep 2

    # 检查日志文件是否存在，不存在则创建一个，避免 tail 报错
    if [ ! -f "$LOG_FILE" ]; then
        touch "$LOG_FILE"
    fi

    echo "----------------------------------------------------------------------"
    echo "  正在实时输出日志 (Ctrl+C 退出跟踪，不会停止程序)  "
    echo "----------------------------------------------------------------------"
    tail -100f "$LOG_FILE"
else
    echo "错误：找不到 $JAR_NAME，无法启动。"
    exit 1
fi

```