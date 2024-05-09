package com.candlk.webapp;

import java.util.ArrayList;
import java.util.List;

public class MarkdownTest {

	public static void main(String[] args) {
		int maxLen = 32;
		List<String> poolNames = new ArrayList<>() {{
			add("TOGETHER");
			add("LFG! Capital");
			add("ICOSharks");
			add("GodBlessChina Capital");
			add("Arbitrum");
			add("Great Pool");
			add("DOUBLETOP_2");
			add("AquaGems");
			add("DOUBLETOP_1");
			add("Sandbar\uD83C\uDFDD\uFE0F");
			add("CryptoTelugu");
			add("XAI-POOL");
			add("XAiverse");
			add("Xaiborg");
			add("ARC community");
			add("ShanyiXAI");
			add("Game Theory");
			add("helloworld");
			add("XMG Capital");
			add("冰蛙 \\| IceFrog");
		}};
		StringBuilder sb = new StringBuilder();
		int offset = 1;
		for (String poolName : poolNames) {
			sb.append(offset).append(". ");
			String format = String.format("%-30s", poolName + "变");
			String s = format.replaceAll(" ", "&nbsp;").replaceAll(poolName, "[" + poolName + "](https://app.xai.games/pool/0x0bb6dd508da137d0e0b7c0d26b4eca824530d854/summary)")
					.replaceAll("变", "<font color=\"warning\">变</font>");
			sb.append(s);
			sb.append("6.72").append(" ".repeat(5))
					.append("×3").append(" ".repeat(5))
					.append("50").append(" ".repeat(5))
					.append("750").append(" ".repeat(5))
					.append("1,029,020").append("  \\n");
			offset++;
		}
		System.out.println(sb);

	}

}
