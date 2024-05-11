package com.candlk.webapp;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.annotation.Resource;

import com.candlk.context.web.Jsons;
import com.candlk.webapp.job.*;
import me.codeplayer.util.RandomUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class XAITest {

	@Resource
	private XAIScanJob xaiScanJob;

	@Test
	public void xaiJobTest() throws Exception {
		xaiScanJob.run();
		Thread.sleep(5 * 60 * 1000);
	}

	@Resource
	private XAIPowerJob xaiPowerJob;

	@Test
	public void xaiPowerJobTest() {
		xaiPowerJob.run();
	}

	@Resource
	XAIRedemptionJob xaiRedemptionJob;

	@Test
	public void xaiRedemptionJobTest() throws Exception {
		xaiRedemptionJob.run();
	}

	@Resource
	Web3JConfig web3JConfig;

	@Test
	public void hasActivePoolTest() throws IOException {
		Web3j web3j1 = Web3j.build(new HttpService("https://arbitrum-one-rpc.publicnode.com"));
		final PoolInfoVO poolInfo = XAIPowerJob.getPoolInfo("0x9a0aa81a7a6c0c82e72b91244bcab051033fa42a", web3j1, true);
		System.out.println(Jsons.encode(poolInfo));

		BigDecimal keysPower = poolInfo.calcKeysPower(BigDecimal.ONE);
		System.out.println(keysPower);
		String delegateOwner = poolInfo.getDelegateAddress(web3j1, true);
		// 0x1b7bc8a49acd1fd9cf7f9cdfb9251230128bb4e2
		System.out.println("代理地址：" + delegateOwner);

		RestTemplate restTemplate = new RestTemplate();
		final BigInteger endBlockNumber = web3j1.ethBlockNumber().send().getBlockNumber(),
				startBlockNumber = endBlockNumber.subtract(new BigInteger("15000")); // 平均 0.26s一个区块，1小时 = 14400 区块，这里取最近15000个区块，1小时内未领奖则算不活跃的池子

		boolean b = XAIPowerJob.getAndFlushActivePool(poolInfo, restTemplate, web3j1, startBlockNumber, web3JConfig.weakActiveThreshold, true);
		// xaiPowerJob.flushActivePoolLocalFile();
		System.out.println("活跃状态：" + b);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(RandomUtil.getInt(10, 999));
			System.out.println(RandomUtil.getInt(1, 2) > 1 ? "❌" : "✅");
		}
	}

}
