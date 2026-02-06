package com.bojiu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.job.StatProfitJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class ScanTest {

	@Test
	public void incrTest() {
		final String address = "0xe13bcf308c470cfa799e110b9953147a33bbb755";
		final BigDecimal amount = new BigDecimal("1000000000000000000");
		List<Object> objects = RedisUtil.execInTransaction(redisOps -> {
			redisOps.execute(new DefaultRedisScript<>(StatProfitJob.INCR_BAKANCE_LUA, Double.class),
					Collections.singletonList("addressBalanceStat:ETH"),
					amount.setScale(6, RoundingMode.HALF_UP).toPlainString(),
					address
			);
		});
		log.info("objects: {}", objects);
	}

}
