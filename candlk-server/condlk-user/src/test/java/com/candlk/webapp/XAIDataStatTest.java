package com.candlk.webapp;

import java.io.File;
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

public class XAIDataStatTest {

	private final static Web3j web3j;

	static {
		// OkHttpClient.Builder okHttpClientBuilder = HttpService.getOkHttpClientBuilder();
		// // okHttpClientBuilder.interceptors().remove(0);
		web3j = Web3j.build(new HttpService("https://arb1.arbitrum.io/rpc"));
	}

	public static void main(String[] args) throws Exception {
		final Triple<BigDecimal, BigDecimal, String> triple = append(23);
		BigDecimal currentCompleteRedemptionAmount = new BigDecimal("838194.655871");
		BigDecimal recycleAmount = new BigDecimal("1257291.983870");
		System.out.println("According to the on-chain data, @XAI_GAMES the last two days add complete redemption amount " + triple.getLeft() + " XAI, recycle amount " + triple.getMiddle() + " XAI.");
		System.out.println("\nUp to now total complete redemption amount " + currentCompleteRedemptionAmount.add(triple.getLeft()) + " XAI, recycle total amount " + recycleAmount.add(triple.getMiddle()) + " XAI.");
		System.out.println("\nsource link: https://arbiscan.io/advanced-filter?tkn=0x4cb9a7ae498cedcbb5eae9f25736ae7d428c9d66&txntype=2&mtd=0x840ecba0%7eComplete+Redemption");
		System.out.println("\nLast transaction hash " + triple.getRight());
		// Pair<BigDecimal, BigDecimal> bigDecimalBigDecimalPair = completeRedemptionAmount();
		// System.out.println("According to the on-chain data, @XAI_GAMES yesterday add complete redemption amount " + bigDecimalBigDecimalPair.getLeft() + " XAI, recycle amount " + bigDecimalBigDecimalPair.getRight() + " XAI.");

		// 解析取消质押的Key数量
		// https://arbiscan.io/advanced-filter?fadd=0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5&tadd=0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5&txntype=0&mtd=0xd4e44335~Create+Unstake+Key+Request&ps=100&p=1
		// parseCreateUnstakeKeyRequest(1, 6, true, "D:\\xai\\unstake\\advanced-filter","0xd1bfecec9df7f67a5771388b76ac881d20b042612e526f5e5dbf7e76ca462e12");

		// 解析取消质押的esXAI数量
		// https://arbiscan.io/advanced-filter?fadd=0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5&tadd=0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5&txntype=0&mtd=0xd4e44335~Create+Unstake+Key+Request%2c0x75710569~Create+Unstake+Es+Xai+Request&ps=100&p=1
		// parseCreateUnstakeKeyRequest(1, 15, false, "D:\\xai\\unstake_esXAI\\advanced-filter", "0xd6b670ba726949b3b82d62f658718685a67ca02af3551bdf02bb5f6f783d5004");
		// 0x64afcb3f7c79fdbf67ff6d41635c95b60dd39fcc   0x1524a4cc9dfa9b748deb10509c329e3e20278c9b
	}

	public static void parseCreateUnstakeKeyRequest(int start, int end, boolean hasKey, String url, String endTxHash) throws Exception {
		final Map<String, BigDecimal> pools = new HashMap<>();
		for (int i = start; i <= end; i++) {
			parseCreateUnstakeKeyRequest2(pools, i, hasKey, url, endTxHash);
		}
		System.out.println(JSON.toJSONString(pools));
		final Map<String, BigDecimal> mappingPools = new HashMap<>(pools.size(), 1F);
		for (Map.Entry<String, BigDecimal> entry : pools.entrySet()) {
			final String contractAddress = entry.getKey();
			mappingPools.put(getContractName(contractAddress) + "（" + contractAddress + "）", entry.getValue());
		}
		final LinkedHashMap<String, BigDecimal> sort = mappingPools.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		System.out.println("映射合约名称：\n" + JSON.toJSONString(sort));
	}

	public static String getContractName(String contractAddress) throws Exception {
		final List<org.web3j.abi.TypeReference<?>> outputReturnTypes = List.of(new org.web3j.abi.TypeReference<Utf8String>() {
		});
		final Function function = new Function("name", Collections.emptyList(), outputReturnTypes);
		org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, contractAddress, FunctionEncoder.encode(function)), DefaultBlockParameterName.LATEST)
				.sendAsync().get();
		final List<Type> results = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
		return ((Utf8String) results.get(0)).getValue();
	}

	public static void parseCreateUnstakeKeyRequest2(Map<String, BigDecimal> pools, int currentId, boolean hasKey, String url, String endTxHash) throws Exception {
		Document doc = Jsoup.parse(new File(url + currentId));
		Elements trs = doc.getElementsByTag("tbody").get(0).getElementsByTag("tr");
		for (final Element tr : trs) {
			Elements elementsByClass = tr.getElementsByClass("text-danger");
			final String txHash = tr.getElementsByClass("advFilterTxHash").get(0).getElementsByTag("a").get(0).text();
			if (endTxHash.equalsIgnoreCase(txHash)) {
				break;
			}
			if (elementsByClass.isEmpty()) {
				Transaction transaction = web3j.ethGetTransactionByHash(txHash).send().getTransaction().get();
				final String input = transaction.getInput();
				String poolAddress = "0x" + input.substring(34, 74);
				BigDecimal keyAmount = new BigDecimal(new BigInteger(input.substring(74), 16));
				if (!hasKey) {
					keyAmount = keyAmount.movePointLeft(18).setScale(2, RoundingMode.HALF_UP);
					final String form = tr.getElementsByClass("advFilterFromAddress").get(0).getElementsByTag("a").get(0).text();
					System.out.println(poolAddress + "\t" + form + "\t" + keyAmount + "\t" + txHash);
				} else {
					System.out.println(poolAddress + "\t" + keyAmount);
				}
				if (keyAmount.compareTo(BigDecimal.ZERO) > 0) {
					pools.merge(poolAddress, keyAmount, BigDecimal::add);
				}
			} else {
				System.out.println("异常交易：" + txHash);
			}
		}
	}

	public static Triple<BigDecimal, BigDecimal, String> append(int currentId) throws Exception {
		BigDecimal total = BigDecimal.ZERO, recycleTotal = BigDecimal.ZERO;
		final String recycleAccount = "0x1F941F7Fb552215af81e6bE87F59578C18783483";
		final String endTxHash = "0x04c74474e3146b95b615a4d4afa133b42596473a2784724bc63ebf3a83374b5c";
		Document doc = Jsoup.parse(new File("D:\\xai\\advanced-filter" + currentId));
		Elements tbody = doc.getElementsByTag("tbody");
		Element body = tbody.get(0);
		Elements trs = body.getElementsByTag("tr");
		String lastTxHash = null;
		for (final Element tr : trs) {
			final String txHash = tr.getElementsByClass("advFilterTxHash").get(0).getElementsByTag("a").get(0).text();
			if (lastTxHash == null) {
				lastTxHash = txHash;
			}
			if (endTxHash.equalsIgnoreCase(txHash)) {
				break;
			}
			String toAddress = tr.getElementsByClass("advFilterToAddress").get(0).getElementsByTag("a").get(0).text();

			Elements advFilterAmount = tr.getElementsByClass("advFilterAmount");
			Elements span = advFilterAmount.get(0).getElementsByTag("span");
			final String text = span.get(0).text().replaceAll(",", "");
			if (recycleAccount.equalsIgnoreCase(toAddress)) {
				recycleTotal = recycleTotal.add(new BigDecimal(text));
			} else {
				total = total.add(new BigDecimal(text));
			}
		}
		return Triple.of(total, recycleTotal, lastTxHash);
	}

	public static Pair<BigDecimal, BigDecimal> completeRedemptionAmount() throws Exception {
		BigDecimal total = BigDecimal.ZERO, recycleTotal = BigDecimal.ZERO;
		final String recycleAccount = "0x1F941F7Fb552215af81e6bE87F59578C18783483";
		for (int i = 1; i <= 4; i++) {
			Document doc = Jsoup.parse(new File("D:\\xai\\advanced-filter" + i));
			Elements tbody = doc.getElementsByTag("tbody");
			Element body = tbody.get(0);
			Elements trs = body.getElementsByTag("tr");
			for (Element tr : trs) {
				String toAddress = tr.getElementsByClass("advFilterToAddress").get(0).getElementsByTag("a").get(0).text();

				Elements advFilterAmount = tr.getElementsByClass("advFilterAmount");
				Elements span = advFilterAmount.get(0).getElementsByTag("span");
				final String text = span.get(0).text().replaceAll(",", "");
				if (recycleAccount.equalsIgnoreCase(toAddress)) {
					recycleTotal = recycleTotal.add(new BigDecimal(text));
				} else {
					total = total.add(new BigDecimal(text));
				}
			}
		}
		return Pair.of(total, recycleTotal);
	}

}
