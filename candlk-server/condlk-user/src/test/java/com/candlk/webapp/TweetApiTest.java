package com.candlk.webapp;

import java.io.File;
import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.*;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.service.*;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class TweetApiTest {

	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;
	@Resource
	TweetWordService tweetWordService;

	static TweetApi tweetApi;

	@BeforeAll
	public static void beforeAll() {
		tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAGq8cOrQ4HTVBBn9Z24umOk8kmik%3DkjB0pGI1V3v3c9WkcQCRVjbfa4DPxJdeTxsF0hWVnIuXrOPVVv",
				"http://127.0.0.1:10809");
	}

	@Test
	public void test() {
		List<String> tweetList = tweetService.lastList(100);
		final String tweetIds = StringUtil.joins(tweetList, ",");
		log.info("推文ID：{}", tweetIds);
		Messager<List<TweetInfo>> tweets = tweetApi.tweets(tweetIds);
		log.info("推文：{}", Jsons.encode(tweets));
	}

	@Test
	public void testSync() {
		final String jsonData = FileUtil.readContent(new File("D:\\tweet2.json"));
		JSONObject data = Jsons.parseObject(jsonData);
		List<TweetInfo> tweets = data.getList("data", TweetInfo.class);
		tweetService.sync(tweets);
	}

	@Test
	public void testSyncUserInfo() {
		final String jsonData = FileUtil.readContent(new File("D:\\tweetUser.json"));
		JSONObject data = Jsons.parseObject(jsonData);
		// 同步用户信息数据
		List<TweetUserInfo> tweets = data.getList("data", TweetUserInfo.class);
		tweetUserService.sync(tweets);
	}

	@Test
	public void testInsertWords() {
		final String wordsData = "[\"airdrop farming\",\"community merge\",\"viral hype\",\"presale frenzy\",\"early adopter\",\"instant rewards\",\"reward boost\",\"memecoin rally\",\"new listings\",\"whale alert\",\"bot protection\",\"launch rush\",\"gasless mint\",\"memecoin explosion\",\"low cap potential\",\"hidden gems\",\"whitelist giveaway\",\"fair tokenomics\",\"early liquidity\",\"stealth presale\",\"founder reveal\",\"community shield\",\"price squeeze\",\"market frenzy\",\"stealth launch\",\"IDO platform\",\"staking platform\",\"trustless launch\",\"anti-dump system\",\"social engagement\",\"NFT crossover\",\"yield boost\",\"token bridge\",\"floor sweeping\",\"meme fusion\",\"launch countdown\",\"degen rewards\",\"presale rewards\",\"public mint\",\"floor accumulation\",\"DEX liquidity\",\"multiwallet sniper\",\"soft launch\",\"community event\",\"community rally\",\"wallet tracker\",\"top gainer\",\"mint party\",\"airdropped tokens\",\"prelaunch buzz\",\"smart mint\",\"team expansion\",\"no team token\",\"ecosystem growth\",\"organic hype\",\"hype train\",\"airdrop rain\",\"trading frenzy\",\"fast track\",\"early rewards\",\"revenue sharing\",\"airdropped NFTs\",\"buyback event\",\"liquidity airdrop\",\"gas optimization\",\"floor racing\",\"multi-chain airdrop\",\"swap aggregator\",\"airdropped governance\",\"silent launch\",\"contract upgrade\",\"NFT integration\",\"future utilities\",\"staking incentives\",\"blockchain expansion\",\"fair vesting\",\"fair airdrop\",\"meme utility\",\"buy wall\",\"sell wall\",\"airdrop storm\",\"liquidity incentives\",\"bridge rewards\",\"staking lockup\",\"governance vault\",\"multi-wallet rewards\",\"trustless pool\",\"yield stacking\",\"deflationary model\",\"staking penalty\",\"flash mint\",\"flash buyback\",\"meme casino\",\"NFT farm\",\"NFT vault\",\"NFT redemption\",\"airdrop ladder\",\"unstoppable meme\",\"unstoppable hype\",\"floor warriors\",\"NFT whales\",\"staking tier\",\"presale protection\",\"launch party\",\"IDO boost\",\"buy pressure\",\"sell pressure\",\"staking pool\",\"liquidity trap\",\"market takeover\",\"viral takeover\",\"early unlock\",\"supply control\",\"circulating supply\",\"fully diluted\",\"supply shock\",\"meme upgrade\",\"ecosystem upgrade\",\"NFT crossover event\",\"NFT adoption\",\"airdrop collector\",\"floor raid\",\"price breakout\",\"pump signal\",\"airdropped whitelist\",\"airdropped rewards\",\"rewards tracker\",\"wallet rewards\",\"floor battle\",\"NFT sweep\",\"meme crossover\",\"floor domination\",\"NFT domination\",\"NFT tracker\",\"staking snapshots\",\"locked liquidity\",\"hyper deflation\",\"liquidity rush\",\"yield domination\",\"NFT governance\",\"NFT inflation\",\"NFT deflation\",\"teamless project\",\"community airdrop\",\"community rewards\",\"whale movements\",\"airdropped memes\",\"multi-chain integration\",\"price ladder\",\"NFT bonding\",\"airdropped staking\",\"memecoin boost\",\"floor support\",\"hype breakout\",\"hype rewards\",\"floor burning\",\"NFT migration\",\"NFT staking\",\"supply raid\",\"token raid\",\"airdropped unlock\",\"vault unlock\",\"launch acceleration\",\"decentralized listing\",\"multiwallet support\",\"buyback race\",\"floor race\",\"airdropped pool\",\"locked rewards\",\"whitelist farming\",\"governance farming\",\"zero gas staking\",\"NFT floor wars\",\"airdrop sniping\",\"token sniping\",\"presale allocation\",\"reward halving\",\"floor halving\",\"NFT halving\",\"teamless token\",\"airdropped bonuses\",\"liquidity bonus\",\"locked airdrop\",\"NFT lockup\",\"staking ladder\",\"airdropped perks\",\"rewards pool\",\"meme farming\",\"airdropped treasures\",\"community treasures\",\"early mover rewards\",\"buyback support\",\"hyper staking\",\"meme raid\",\"NFT rewards vault\",\"airdrop vault\",\"staking vault\",\"floor buying\",\"NFT buying\",\"governance portal\",\"staking portal\",\"NFT portal\",\"liquidity vault\",\"vault rewards\",\"meme rewards\",\"airdropped memes\"]";
		List<String> words = Jsons.parseArray(wordsData, String.class);
		Date now = new Date();
		List<TweetWord> tweetWords = CollectionUtil.toList(words, w -> new TweetWord(w, TweetWord.TYPE_HOT, now));
		tweetWordService.saveBatch(tweetWords);
		String t = "对推文进行分词要求：1、分词并生成英文的代币名称和大写的代币简称。2、以json格式{\"words\":[\"\"],\"name\":\"\",\"symbol\":\"\"}输出，不要输出任何额外信息。推文：remember when we hated bundlers so much we damn near celebrated them getting drained because we considered them “scammers” and “ruggers”now we support streamers and KOLs that openly record themselves bundling.";
	}

}

