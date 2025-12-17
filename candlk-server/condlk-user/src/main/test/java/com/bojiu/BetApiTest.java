package com.bojiu;

import javax.annotation.Resource;

import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.job.GameBetJob;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class BetApiTest {

	@Resource
	GameBetJob gameBetJob;

	@Test
	public void getGameBetsTest() {
		BetProvider type = BetProvider.D1CE;
		BetApi api = BetApi.getInstance(type);
		gameBetJob.doQueryAndSyncGameBetsForSingleVendor(api);
	}

}
