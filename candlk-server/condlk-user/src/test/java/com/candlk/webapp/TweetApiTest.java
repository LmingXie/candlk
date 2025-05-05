package com.candlk.webapp;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.*;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndex;
import com.candlk.webapp.user.service.*;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
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
	@Resource
	ESEngineClient engine;

	static TweetApi tweetApi;

	@BeforeAll
	public static void beforeAll() throws IOException {
		tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAGq8cOrQ4HTVBBn9Z24umOk8kmik%3DkjB0pGI1V3v3c9WkcQCRVjbfa4DPxJdeTxsF0hWVnIuXrOPVVv",
				"http://127.0.0.1:10809");
	}

	@Test
	public void test() {
		List<String> tweetList = tweetService.lastList(100);
		final String tweetIds = StringUtil.joins(tweetList, ",");
		log.info("推文ID：{}", tweetIds);
		// Messager<List<TweetInfo>> tweets = tweetApi.tweets(tweetIds);
		// log.info("推文：{}", Jsons.encode(tweets));
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
	public void testInsertWords() throws Exception {
		List<String> words = Arrays.asList(
				"floor racing", "liquidity incentives", "market trends", "buyback event", "NFT tracker", "memecoin boost", "buy limit", "tokenization", "launch acceleration", "shit liquidity", "token sniping", "farm", "gas optimization", "low cap", "community governance", "upgrade", "narrative", "pool", "community", "liquidity_pool_rewards", "mfer", "metrics", "whale manipulation", "trustless launch", "long squeeze", "utility", "KOALA AI", "governance token", "farming_pool", "miner", "dollar", "crosschain", "viral takeover", "twitter raid", "blockchain_technology", "airdrop ladder", "listing", "trading bot", "mining_fees", "low float", "yield_farming_protocol", "address", "supply shock", "proof of work", "uniswap", "dev team?", "dapp_rewards", "wen lambo", "teamless token", "shit moon", "community airdrop", "decentralized exchange", "sale", "blockchain_development", "x", "staking yield", "collateral", "staking_return", "yield domination", "woke token", "mining algorithm", "crypto_wallet", "on-chain governance", "crypto twitter", "no team token", "buyback support", "short squeeze", "floor burning", "Degen Spartan AI", "protocols", "frogs", "meme culture", "dapp development", "chainlink", "cryptospace", "reward boost", "NFT rewards vault", "cryptography", "network_token", "100x", "ape in", "for the culture", "liquidity provider", "trustworthy project", "not audited", "whale wallet", "NFT inflation", "reward_token", "non_fungible", "exchange", "block_data", "Pudgy Penguins", "Telegram group", "dex aggregators", "joke token", "trade execution", "solana", "price prediction", "call group", "blockchain_size", "crypto wallets", "layer1", "layer2", "block_size", "wallet_address", "decentralized finance", "proof_of_work", "blockchain oracle", "trading volume", "cross_chain_transfer", "market maker", "miner_rewards", "We Love Tits", "sharding", "Smoking Chicken Fish", "governance vault", "liquidity_rebalancing", "testnet", "influencer push", "utility token", "platform_token", "staking platform", "uniswap_swap", "synth tokens", "NFT adoption", "eos", "giga", "fast transaction", "0 tax", "whitelist farming", "smart contract", "hyper deflation", "crypto_community", "token burn", "block", "airdropped whitelist", "litecoin", "partnership", "early adopter", "ngmi", "validator_rewards_pool", "blockchain protocol", "apes", "1000x", "MAD", "viral hype", "bridge rewards", "digital assets", "swap aggregator", "crypto wallet", "max supply", "early mover rewards", "public key", "MAX", "ghost project", "smart contract tokens", "meme battle", "rekt moment", "instant", "holders", "memewave", "shit playbook", "supply control", "yield", "airdrop farming", "Zerebro", "deflationary model", "layer 2 solution", "flash buyback", "fast rug", "nft_auction", "marketplace liquidity", "communitycoin", "erc721", "blockchain_developer", "cat in a dogs world", "shit gang", "stablecoin rewards", "liquidity rush", "crypto staking", "governance", "capital", "transaction speed", "dogwifhat", "shit play", "pippin", "shit lp", "blockchain_network", "RETARDIO", "crypto asset management", "multi-wallet rewards", "floor buying", "gas_fee", "block_height", "sol shitcoin", "Butthole Coin", "yield_rewards", "cross-chain tokens", "community driven", "decentralized_finance", "crypto_projects", "swap", "nfa", "Dolan Duck", "Pigeon Tech", "network_fee", "M3M3", "army", "liquidity pool", "coinbase", "catwifhat", "price chart", "shitcoin", "twitter shill", "Schizo Terminal", "pepe vibes", "zoomers", "early entry", "pool_rewards", "fund_manager", "liquidity vault", "buy the dip", "nft_market", "smartcontract", "shitcoin watcher", "hard cap", "vault rewards", "exchange volume", "liquidity protocol", "meme DAO", "trending now", "compliance token", "tokens", "dump", "floor", "influencer shill", "burn", "transaction_history", "Maneki", "staking lockup", "early bird", "shitcoin vibes", "tokenomics model", "burn event", "decentralized_staking", "vitalik", "staking_pool", "staking_fee", "liquidity_pool", "validator_pool", "lambo", "shit raid", "price discovery", "blockchain transactions", "public_key", "vibe", "defi_stake", "dextools", "airdrop farm", "supply", "dex screener", "NFT deflation", "token_swap", "pool_investment", "gasless transaction", "memecoin rally", "token_migration", "whale watch", "dog dev", "memecoin gem", "frontrun", "community fund", "stablecoin", "public_chain", "deposit", "sniper bot", "fomo entry", "Boba Oppa", "Top Hat", "NFT floor wars", "NFT portal", "Samoyedcoin", "zero dev", "blockchain investment", "defi_farming_rewards", "hash rate", "market analysis", "supply raid", "degen play", "shit narrative", "blockchain scalability", "x space", "token_burn", "doxxed", "dog vibes", "project roadmap", "decentralized_storage", "shitposting", "fast track", "dividend", "liquidity bonus", "NFT lockup", "crypto_exchange", "litecoin_rewards", "mining_data", "synthetics", "deflation", "shit strategy", "holder", "team", "early access", "call", "marketing", "whale trap", "consensus mechanism", "automated", "ICO", "shit", "multi-chain integration", "community merge", "blockchain companies", "stealth presale", "anti-dump system", "rug", "doge", "liquidity", "IDO", "anti-rug", "user incentive", "Hege", "Daddy Tate", "nft_community", "pepe", "degen", "community call", "block explorer", "unlock", "mining_rewards", "fair vesting", "nft_marketplace", "meme war", "CEX listing", "difficulty", "memecoin", "IEO", "fun project", "sell pressure", "Moo Deng", "shit trader", "deflationary", "Fartboy", "cryptocurrency_exchange", "community rewards", "rugpull", "reflections", "shit floor", "mining_pool", "contract upgrade", "shit mint", "shit shill", "scalability", "Popcat", "blockchain innovation", "decentralization", "NFT crossover event", "with no use", "shit vibes", "early", "Ben the Dog", "consensus layer", "presale frenzy", "paper hands", "NFT farm", "election", "nft_trading", "soft launch", "rekt", "floor accumulation", "whale movements", "shit dev project", "zero_knowledge", "yield generator", "hype rewards", "sol shit", "p2p", "limit order", "ecosystem upgrade", "launch rush", "fund", "mint party", "hype", "defi_tokens_pool", "defi ecosystem", "cross-chain compatibility", "shitcoin tracker", "floor warriors", "yield boost", "circulating supply", "twitter", "Comedian", "launch party", "season", "moonshot", "degods", "liquidity_locker", "staking_tokenomics", "crowdsale", "private key", "coin_market_cap", "moon", "price breakout", "staking ladder", "staking farming", "shitcaller", "funny token", "Wen", "digital_asset", "PONKE", "digital_token", "NFT domination", "tokenomics", "project whitepaper", "crypto asset allocation", "roadmap", "blockchain expansion", "smart", "TikTok challenge", "whitelist giveaway", "Web3", "degen rewards", "shit beta", "ledger_address", "ledger", "wealth", "chain", "web3 wallet", "venture capital", "funding", "wallet", "wallet security", "chad", "secure wallet", "crypto orders", "degen call", "staking_address", "market cap", "mint price", "airdropped staking", "private blockchain", "ethereum_classic", "lending_protocol", "0.1 mcap", "shit dev", "zero utility", "satire", "market takeover", "quick", "wagmi", "blockchain voting", "trustless pool", "validator node", "Ava AI", "new listings", "block_time", "NFT vault", "protocol", "shitpost", "dao", "whale", "unstoppable meme", "LAIKA the Cosmodog", "drop incoming", "blockchain identity", "marketplace", "stealth launch", "volume spike", "supply cap", "Discord community", "staking_token", "nft_collectibles", "shitlist", "market", "deflationary_token", "Official Trump", "not financial advice", "raid", "option", "launchpad", "airdropped memes", "revenue sharing", "fair launch", "crypto_technology", "airdropped", "meme army", "vibes only", "decentralized_system", "presale rewards", "viral marketing", "blockchain startup", "wif", "community-driven", "crypto space", "social media", "memecoin explosion", "meme trends", "uniswap_v3", "platform_rewards", "zoom", "block_explorer_data", "founder reveal", "multiwallet sniper", "presale whitelist", "community treasures", "cross-chain swap", "block verification", "360noscope420blazeit", "hodl", "LP tokens", "staking pool", "NFT redemption", "FOMO", "no roadmap", "voting power", "degen ape", "memefarm", "dev", "NPC On Solana", "dex", "synthetic tokens", "fiat", "locked liquidity", "validator rewards", "GME", "crypto marketplace", "public blockchain", "network_tokens", "minting tools", "gang", "sell wall", "meme fusion", "staking farm", "cross-chain", "NFT drop", "time lock", "private_key", "sushiswap", "memepool", "nft_staking", "exchange_token", "staking_community", "transaction history", "MichiCoin", "airdrop storm", "multisig wallet", "ai16z", "price ladder", "call dump", "mint", "crypto funds", "DEX listing", "shit liquidity drain", "airdropped rewards", "farming", "liquidity_provider", "airdropped NFTs", "layer 2", "yield aggregator", "Spinning Cat", "token lock", "DAO governance", "block miner", "airdropped governance", "hype breakout", "floor sweeping", "shitcoin play", "presale", "ct", "cryptocurrency_miners", "NFT staking", "borrow", "Opus", "crypto_protocol", "decentralized listing", "airdropped bonuses", "scalable", "defi_protocol", "Slerf", "asset management", "no utility", "dip", "bot protection", "ethereum", "hype train", "Slothana", "staking_rewards", "chain_analysis", "decentralized_exchange", "liquidity trap", "snipe bot", "community token", "governance_token", "meme crossover", "soft cap", "blockchain tech", "shitcoin game", "crypto_fund", "asset_token", "community engagement", "cryptocurrency", "to the moon", "smart mint", "dev wallet", "memelist", "whales club", "token_sale", "shiter", "shitguide", "financial inclusion", "BOOK OF", "mainnet", "shit scam", "total supply", "NFT integration", "collector", "asset_management", "coin burn", "random", "liquidity_staking", "viral posts", "earn yield", "Eliza", "crosschain_rewards", "meme rewards", "alpha", "layer 1 solution", "mining reward", "emissions", "protocol_fees", "market efficiency", "alpha leak", "shit launch", "digital", "stablecoins", "rugproof", "price squeeze", "digital_wallet", "gm", "elon", "oracle_node", "burned tokens", "community rally", "prelaunch", "private sale", "LEA AI", "developer", "shit coin", "telegram call", "memecoin rotation", "bitcoin", "withdraw", "shit only up", "cryptocurrency adoption", "meme", "shit only", "token bridge", "rewards tracker", "nft_collectible", "airdrop collector", "model", "trade pairs", "renounced", "repost", "trading pairs", "IDO platform", "asset_class", "consensus protocol", "meme raid", "defi", "whitelist", "it", "degen vibes", "Myro", "stop loss", "low market cap", "nft_sale", "community shield", "liquidation event", "yield_farming", "shitcoin culture", "trading", "project liquidity", "liquidity airdrop", "vault unlock", "liquidity_mining", "airdrop sniping", "funding round", "crypto portfolio", "memelord", "cult", "floor halving", "shitdump", "shitcoin sniper", "Big Dog Fink", "crypto trading", "x influencer", "botted", "creator", "faucet", "digital currencies", "onchain", "community event", "meme index", "fud", "protocol governance", "teamless project", "shiba", "proof of authority", "staking model", "transaction", "floor support", "IDO boost", "fast money", "hashing", "yield stacking", "early rewards", "clown dev", "networking", "hidden gems", "automated_market_maker", "Gigachad", "sol", "vesting", "liquidity tokens", "airdropped perks", "proof_of_stake", "smart_contract_developer", "NFT", "NFT crossover", "incoming", "decentralized application", "gasless mint", "NFT migration", "Pundu", "crypto exchanges", "vibe token", "transaction confirmation", "fully diluted", "crypto_trader", "proof of stake rewards", "reward halving", "shit project", "sell limit", "technical analysis", "organic hype", "hodl_rewards", "layer_2", "governance farming", "airdrop_rewards", "just vibes", "yield farming", "block_difficulty", "meme season", "seed round", "viral coin", "governance portal", "memetic", "floor battle", "airdropped tokens", "Pwease", "meme contest", "silent launch", "block_analysis", "peer_to_peer", "transaction_fee", "wallet_balance", "rewards distribution", "wen moon", "order book", "compound", "honeypot", "stake", "liquidity rewards", "McDull (Meme)", "circulation", "yield farming platform", "blockchain transparency", "viral launch", "staking vault", "high APY", "DEX liquidity", "staking incentives", "crosschain_farming", "meme vault", "trending", "eth bridge", "multisig", "Melania", "NFT governance", "exchange listing", "governance_tokenomics", "floor raid", "LFG", "hashrate", "stakeholder", "unstoppable hype", "SolanaPay", "meme utility", "web3 integration", "Just a chill guy", "shit rugfree", "block_rewards", "top gainer", "Goatseus Maximus", "staking rewards", "based", "cross_chain", "wallet address", "decentralized", "market frenzy", "nft_minting", "staking_rate", "node", "decentralized project", "staking_platform_rewards", "bonk vibes", "whitepaper release", "block_data_storage", "viral", "bridge", "crypto_token", "protocol_analysis", "locked airdrop", "shitcoin hunter", "scam", "rewards pool", "staking tier", "kek", "developer wallet", "x influencer shill", "audit", "deflationary token", "pump", "smart contract auditing", "offensive", "cryptocurrency_trader", "shitposter", "launch", "presale protection", "bridge solution", "atomic_swap", "flash loan", "staking penalty", "meme casino", "ATH", "block_explorer", "NFT sweep", "reward_rate", "Doland Tremp", "validator_tokens", "locked lp", "validator", "project", "block_reward", "meme revolution", "arbitrage", "sniper", "Unicorn Fart Dust", "roadmap soon", "woof", "blockchain assets", "defi staking", "social engagement", "sniped", "shit farm", "charity donation", "team expansion", "oracle", "zk_rollup", "airdrop rain", "Fwog", "staking_apy", "ecosystem growth", "proof of stake", "meme upgrade", "liquidity lock", "blockchain", "trading frenzy", "launch countdown", "rewards", "coin", "future utilities", "node_rewards", "drop", "LOCK IN", "lp burned", "swap_token", "presale allocation", "hyper staking", "buy wall", "decentralized_apps", "buyback", "fair tokenomics", "auction", "shitlaunching", "airdropped unlock", "interest", "p2p_exchange", "gas", "shitcoin guide", "sol shitseason", "inflationary token", "mining farm", "fudding", "shit alpha drop", "reward staking", "zero tax", "technology", "Bonk", "copypasta token", "airdropped treasures", "comment", "memecall", "ecosystem", "trend", "NFT buying", "fair", "crypto_farming", "community growth", "locked rewards", "liquidity_farming", "Bozo Benk", "price", "governance_model", "wallet rewards", "jeet", "proof", "slippage", "price action", "pump signal", "mad lads", "block reward", "lending", "Shoggoth", "buy pressure", "contract", "dividend token", "blockchain bridge", "fair airdrop", "MUMU THE BULL", "dApp", "nft_royalties", "minting", "shit coinbase", "infinite supply", "memetoken", "nft_project", "degens", "blockchain explorer", "digital tokens", "sol drop", "scam alert", "floor domination", "liquidity_incentives", "distribution", "unknown dev", "gem", "staking", "undoxxed", "rug pull", "shit dump", "reward", "liquidity_farming_rewards", "NFT marketplace", "staking_fees", "cross-promotion", "staking_platform", "memes", "memer", "shit viral", "token", "crypto transaction fees", "culture", "transaction fees", "validator_fees", "zero gas staking", "snipe tool", "raid tweet", "instant rewards", "asset", "swap_pool", "floor race", "magic", "crosschain_swap", "crypto market", "validator_rewards", "project audit", "Fartcoin", "staking_contract", "multiwallet support", "multi-chain", "NFT bonding", "public mint", "digital_currency", "shitlaunch", "development", "reward system", "community reward", "safu", "whale alert", "defi_tokens", "airdropped pool", "mining", "proof of concept", "airdrop vault", "Mother Iggy", "NFT halving", "crypto", "Sillynubcat", "multi-chain airdrop", "wallet tracker", "smart_contract", "angel investors", "token raid", "defi_stablecoin", "anti-whale", "liquidity_token", "staking portal", "platform", "early unlock", "diamond hands", "network", "dev gone", "erc20", "metaverse", "Harambe", "security", "swap_fee", "market_cap", "NFT whales", "early liquidity", "high gas fees", "cat", "ethereum_network", "flash mint", "prelaunch buzz", "value", "low cap potential", "token_holder", "shit alert", "Vine", "holder distribution", "shit alpha", "staking snapshots", "airdrop", "trader", "Twitter campaign", "liquidity mining", "SNAP", "safe launch", "blockchain venture", "blockchain testing", "nft_tokens", "blockchain projects", "delegation", "supply_rate", "buyback race", "narrative coin", "meme farming"
		);

		Date now = new Date();
		List<TweetWord> tweetWords = CollectionUtil.toList(words, w -> new TweetWord(w, TweetWord.TYPE_HOT, now));
		tweetWordService.saveBatch(tweetWords);
		engine.bulkAddDoc(ESIndex.KEYWORDS_INDEX, tweetWords);
		// String t = "对推文进行分词要求：1、分词并生成英文的代币名称和大写的代币简称。2、以json格式{\"words\":[\"\"],\"name\":\"\",\"symbol\":\"\"}输出，不要输出任何额外信息。推文：remember when we hated bundlers so much we damn near celebrated them getting drained because we considered them “scammers” and “ruggers”now we support streamers and KOLs that openly record themselves bundling.";
	}

	@Test
	public void calcScoreTest() {
		final String text = "We were clearly telling you sub-10M folks this was going to happen with Housecoin.";
		List<Term> segment = NotionalTokenizer.segment(text);
		log.info("分词结果：{}", StringUtil.join(segment, term -> term.word, " | "));
	}

	@Test
	public void testSyncWords() throws Exception {
		List<TweetWord> all = tweetWordService.findAll();
		engine.bulkAddDoc(ESIndex.KEYWORDS_ACCURATE_INDEX, all);
	}

	@Test
	public void testDelWords() throws Exception {
		List<TweetWord> all = tweetWordService.findByWords(Arrays.asList(
				"launch", "sol", "solana"
		));
		int i = engine.batchDelByIds(ESIndex.KEYWORDS_ACCURATE_INDEX, CollectionUtil.toList(all, t -> t.getId().toString()));
		log.info("删除了{}个词", i);
		tweetWordService.deleteByIds(CollectionUtil.toList(all, TweetWord::getId));
	}

}

