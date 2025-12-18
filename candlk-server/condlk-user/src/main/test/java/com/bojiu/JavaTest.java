package com.bojiu;

import java.util.List;

import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.bet.impl.KyBetImpl;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.dto.HedgingDTO.Odds;
import com.bojiu.webapp.user.dto.HedgingDTO.Out;
import com.bojiu.webapp.user.utils.HGOddsConverter;
import org.junit.jupiter.api.Test;

public class JavaTest {

	@Test
	public void oddsTest() {
		double[] result = HGOddsConverter.convertOddsRatio(0.560, 1.340, 2, null);
		System.out.println("H=" + result[0] + ", C=" + result[1]);
	}

	@Test
	public void hedgingTest() {
		HedgingDTO dto = new HedgingDTO(new Odds[] {
				new Odds(2.06, 1.85),
				new Odds(2.08, 1.9),
				new Odds(2, 1.82)
		});
		System.out.println("综合赔率：" + dto.overallOdds());
		System.out.println("A平台 串子投注金额：" + dto.getAInCoin());
		System.out.println("A平台 返水金额（串子全输时）：" + dto.getARebateCoin());
		System.out.println("A平台 串关全输时的固定收益：" + dto.getLoss());
		System.out.println("A平台 串关全赢时的固定收益（按输赢金额计算返水）：" + dto.getWin());

		double[] hedgingCoins1 = dto.getHedgingCoins();
		System.out.println("最佳对冲下注方案：" + Jsons.encode(hedgingCoins1));
		List<Out> profitResults = dto.calcProfit(hedgingCoins1);
		System.out.println(Jsons.encode(profitResults));
	}

	@Test
	public void kyDecodeDPDataTest(){
		final String rawData = "H4sIAAAAAAAAAO1aW28ctxX+K8I86Gklz221uwKEQpIty4l1iVdWGgdGMZqhdsc7Nw05u1oZApKm\\nT0Hemj4kCIwmQYEAboHGTVvZRpEHA/0dayC1239RXmZIjvZoLaAo4KICbIlzeEgenu+cw280fGh4\\nPgmHIRnfCoxlw2jI59shJsZyUkQRlQVBSMI0sYXKgYcHiKx5UbTrY2PZpJK+sWyxX+tZ2dgbl417\\nOW/4VPvA8wd380hM4ve9OLuDcHcUEp8ONywhJHfSNK6EdGo/TQ7D3v6OsfzQ8LLsOjr0ioiIyb0k\\nQbmc8SBNB2uo7KLtLS+nZmpTnReu8X0cUMvKleLMI7fTXqqM5ItveXRXD08bxmEYEZTfRl6vQEJB\\nSLpZmhMh6Dd1C8MkChP0fhiQfuXLiI+urRKlXlATxNzIdb74u4h60sgRRvkQBXR/JEx65abqmvte\\nxMyyKqk0xGSS4y7KQ4S3i5iqMEmYaBK7YSSpH/poz+vxKTK/ZlGWp4cIV52YeEng5QF/pkNJSCLm\\nkJ/+9ut//vLJwk/Pvnz1w5N/fPXd62c/CtHrx49ef/bVq09/8+pP3xmlvvCqQUZ04L8efy4GPioH\\n/vVLIXr9+C/6wGFIlff6L59/Mkf6Xjo3tzC33X/x2zn/xdch+zHn//2PL59/69NeqUPyl8+/p5Ji\\n/PLZj8ncev/FN8Xci4+ZGSzsOHaYTjQ5+3hy9mRy9u3k6UeTs88nZz9Mzr6enD2anH0/OfuC9/55\\ncvZ7rvMH/u+LydNfTc5+xxufTZ5+whsfsS7281Ou/w1dByVynYVVHHrJ3A6Pi7mbHkWhN7cbeeQw\\nzWOqetK/tBtPmTsClGo4UZEvEtDPVrOwbG02y8auXza6wUC0cuQRRK1A1IRxPROLPEeJP75DFZho\\n0WTCYcogwzxuWMtPA1QOoDvhsXB0SNdtmiZrUW2bt9JEBN0RZkaZXMa8b4nWiI2gxmPqDRZIMyfW\\nZlZTaxOqGc1TOmcgsmAnCPBOxooYNdyhmHh5NKbxK+rBbTREEauAIkcR1sQ8fw69YZqHBK0VhLAV\\nSV6gS9WDwzxNyD7KcciGGQ4V9WhFNMa8kRZJgPKLzGD9Ge3kpdnixWWNl7wKKYuJNuorcDUK8Dqr\\nr1TEgguVqlSrO65XXC5kQ6WUAR0mq+UxYCwfehGmew3xNhrdpdBrkp2MhzYfgfcQJlsopyU8ETUn\\n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";

		try {
			String result = KyBetImpl.decryptPBData(rawData);
			System.out.println("--- 解密后的 JSON 字符串 ---");
			System.out.println(result);
			// 接下来你可以使用 Jackson 的 ObjectMapper().readTree(result) 解析成对象
		} catch (Exception e) {
			System.err.println("解密失败: " + e.getMessage());
		}
	}
}