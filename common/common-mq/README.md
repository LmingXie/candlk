# common-mq

基于 阿里云 RocketMQ 的 抽象封装库。

### 代码参考

#### 【生产者】发送消息到 MQ

```java
import javax.annotation.Resource;

import MqProducer;
import org.springframework.stereotype.Component;

@Component
public class MqProducerDemo {

	@Resource
	MqProducer mqProducer;

	public void test() {
		// mqProducer.send("数据载体", "MQ描述符对象", 0);
	}

}
```

#### 【消费者】消费 MQ 消息

```java
import javax.annotation.Resource;

import MqConsumer;
import com.aliyun.openservices.ons.api.Action;
import org.springframework.stereotype.Component;

@Component
public class MqConsumerDemo implements MqConsumer {

	@Override
	public MqTagDescriptor getSubscription() {
		// return MQ描述符对象;
	}

	@Override
	public Action doConsume(Message message, ConsumeContext context) {
		// TODO 消费消息
		return Action.CommitMessage;
	}

}
```

### 配置参考

```yaml
rocketmq:
  accessKey: <accessKey>
  secretKey: <secretKey>
  nameSrvAddr: http://MQ_INST_1728488154455335_BXyTuwqG.mq-internet-access.mq-internet.aliyuncs.com:80
```
