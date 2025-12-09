package com.bojiu;

import com.bojiu.webapp.user.utils.HGOddsConverter;
import org.junit.jupiter.api.Test;

public class JavaTest {

	@Test
	public void oddsTest() {
		double[] result = HGOddsConverter.convertOddsRatio(0.560, 1.340, 2, null);
		System.out.println("H=" + result[0] + ", C=" + result[1]);
	}

}