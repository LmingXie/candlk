package com.bojiu;

import java.util.List;

import com.bojiu.context.web.Jsons;
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

}