package com.candlk.webapp;

import java.io.IOException;
import javax.annotation.Resource;

import com.candlk.context.web.Jsons;
import com.candlk.webapp.job.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
	}

	@Resource
	private XAIPowerJob xaiPowerJob;

	@Test
	public void xaiPowerJobTest() {
		xaiPowerJob.run();
	}

	@Test
	public void hasActivePoolTest() throws IOException {
		Web3j web3j1 = Web3j.build(new HttpService("https://arbitrum-one-rpc.publicnode.com"));
		final PoolInfoVO poolInfo = XAIPowerJob.getPoolInfo("0x85343b66e70a24853083a1c15cea27685c927e6f", web3j1, true);
		System.out.println(Jsons.encode(poolInfo));

		String delegateOwner = poolInfo.getDelegateAddress(web3j1, true);
		// 0x1b7bc8a49acd1fd9cf7f9cdfb9251230128bb4e2
		System.out.println("代理地址：" + delegateOwner);

		// RestTemplate restTemplate = new RestTemplate();
		// final BigInteger endBlockNumber = web3j1.ethBlockNumber().send().getBlockNumber(),
		// 		startBlockNumber = endBlockNumber.subtract(new BigInteger("15000")); // 平均 0.26s一个区块，1小时 = 14400 区块，这里取最近15000个区块，1小时内未领奖则算不活跃的池子
		//
		// boolean b = xaiPowerJob.getActivePool(poolInfo, restTemplate, startBlockNumber, true);
		// xaiPowerJob.flushActivePoolLocalFile();
		// System.out.println("活跃状态：" + b);
	}

}
