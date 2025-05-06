package com.candlk.webapp;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.DeepSeekApi;
import com.candlk.webapp.api.DeepSeekChat;
import com.candlk.webapp.es.ESEngineClient;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class DeepSeekApiTest {

	static DeepSeekApi deepSeekApi = DeepSeekApi.getInstance();
	static ESEngineClient esEngineClient;

	@BeforeAll
	public static void init() throws IOException {
		esEngineClient = new ESEngineClient(
				"https://127.0.0.1:9200",
				"008d29b8ca1324053f3cc196ae88e9e2dc865463053f0880f9c5cf708588f818",
				"elastic", "YqR6mL+USUyJnPYPKtG="
		);
	}

	@Test
	public void chat() {
		final String tweet = "Quick Alpha \uD83D\uDD25 \n"
				+ "The @Fleek Token sale is now live on @CoinList and it’s one of the most exciting community rounds I’ve seen this year.\n"
				+ "\n"
				+ "This is your shot to get in early — at better terms than private investors — with full liquidity on Day 1. No cliffs. No lockups. Just upside.\n"
				+ "\n"
				+ "Just in case you are wondering what Fleek is, it’s the agentic cloud powering ai agents and virtual influencers\n"
				+ "- 100% unlock at TGE\n"
				+ "- $75M FDV\n"
				+ "- 60% discount to last private round\n"
				+ "- $100 min, $250,000 max\n"
				+ "- Sale closes May 8, 2025 at 17:00 UTC\n"
				+ "\n"
				+ "Don’t wait — most CoinList sales oversubscribe fast.\n"
				+ "Own a stake in the agentic future: https://t.co/N0fSEToVlR\n"
				+ "Fleek is the first cloud platform purpose-built for agents — not apps.\n"
				+ "Think Vercel + Shopify + Stripe, but instead of launching dashboards or storefronts, you launch autonomous agents that run 24/7, adapt in real-time, and monetize themselves. \n"
				+ "\uD835\uDC68\uD835\uDC8F\uD835\uDC85 \uD835\uDC89\uD835\uDC86\uD835\uDC93\uD835\uDC86'\uD835\uDC94 \uD835\uDC98\uD835\uDC89\uD835\uDC86\uD835\uDC93\uD835\uDC86 \uD835\uDC8A\uD835\uDC95 \uD835\uDC88\uD835\uDC86\uD835\uDC95\uD835\uDC94 \uD835\uDC8A\uD835\uDC8F\uD835\uDC95\uD835\uDC86\uD835\uDC93\uD835\uDC86\uD835\uDC94\uD835\uDC95\uD835\uDC8A\uD835\uDC8F\uD835\uDC88 .. Fleek's upcoming Agent Marketplace will let creators list, promote, and earn from their AI agents and virtual influencers — through subs, token-gated content, brand deals, and more.\n"
				+ "It’s not just infra. It’s a monetization engine.\n"
				+ "\uD835\uDC16\uD835\uDC21\uD835\uDC32 \uD835\uDC2D\uD835\uDC21\uD835\uDC22\uD835\uDC2C \uD835\uDC26\uD835\uDC1A\uD835\uDC2D\uD835\uDC2D\uD835\uDC1E\uD835\uDC2B\uD835\uDC2C ? \n"
				+ "Most AI infra today is fragmented. Building an agent means patching together 10+ tools across LLMs, hosting, media generation, and billing.\n"
				+ "Fleek brings it all together in one seamless platform.\n"
				+ "With Fleek, you get:\n"
				+ "➠ LLM + TEE hosting\n"
				+ "➠ Persistent memory & compute\n"
				+ "➠ Voice, image, video generation\n"
				+ "➠ Plugin support\n"
				+ "➠ Native monetization tools\n"
				+ "➠ A marketplace to scale and monetize agents\n"
				+ "\uD835\uDC12\uD835\uDC28 \uD835\uDC30\uD835\uDC21\uD835\uDC1A\uD835\uDC2D \uD835\uDC1D\uD835\uDC28\uD835\uDC1E\uD835\uDC2C $\uD835\uDC05\uD835\uDC0B\uD835\uDC0A \uD835\uDC1D\uD835\uDC28?\n"
				+ "→ Stake to unlock access + platform credits \n"
				+ "→ Boost discoverability in the marketplace \n"
				+ "→ Enable agent monetization & premium features \n"
				+ "→ Burn via platform and marketplace fees \n"
				+ "→ Run infra, settle disputes, power the ecosystem\n"
				+ "Partners: ElizaOS, AGIXT, Venice AI, Story Protocol, deBridge\n"
				+ "The infra is working. The ecosystem is growing.\n"
				+ "Fleek is building the platform — and $FLK is your way in.\n"
				+ "Sale ends May 8. Don’t miss it.  #FLK #Fleek #CoinList #AgentEconomy, NFA / DYOR";

		List<Term> segment = NotionalTokenizer.segment(tweet);

		Set<String> words = new HashSet<>(segment.size());
		for (Term term : segment) {
			// 字符必须大于1 && 不包含在内部停用词中
			if (term.word.length() < 2 || esEngineClient.stopWordsCache.contains(term.word)) {
				continue;
			}
			words.add(term.word);
		}
		Messager<DeepSeekChat> chat = deepSeekApi.chat("根据推文生成代币名称和代币符号，仅输出json格式的{\"name\":\"\",\"symbol\":\"\"}。" + StringUtil.joins(words, " "));
		if (chat.isOK()) {
			DeepSeekChat data = chat.data();
			if (CollectionUtils.isNotEmpty(data.choices)) {
				final String content = data.choices.get(0).message.content;
				final String fixedText = content.replaceAll("```json\\n", "").replaceAll("```", "").replaceAll("\\n", "");
				if (JSON.isValid(fixedText)) {
					System.out.println(Jsons.encode(Jsons.parseObject(fixedText)));
				}
			}
		}
	}

}
