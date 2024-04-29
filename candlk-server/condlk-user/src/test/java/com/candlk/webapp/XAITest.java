package com.candlk.webapp;

import javax.annotation.Resource;

import com.candlk.webapp.job.XAIPowerJob;
import com.candlk.webapp.job.XAIScanJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

}
