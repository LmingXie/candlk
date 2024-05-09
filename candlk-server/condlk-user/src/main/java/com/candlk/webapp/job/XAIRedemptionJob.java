package com.candlk.webapp.job;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.util.Formats;
import com.candlk.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.NumberUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class XAIRedemptionJob {

	@Resource
	private Web3JConfig web3JConfig;
	public final static NumberFormat format = DecimalFormat.getCurrencyInstance(Locale.US);

	@Scheduled(cron = "${service.cron.XAIRedemptionJob:0 0 1 * * ?}")
	public void run() throws Exception {
		final EasyDate d = new EasyDate();
		final String yyyyMMdd = Formats.getYyyyMMdd(d) + "", yyyyMM = Formats.getYyyyMM(d) + "", weeklyYyyyMMdd = Formats.getYyyyMMdd(d.addDay(1 - d.getWeekDay())) + "";
		final JSONObject root = deserialization(web3JConfig.statFilePath);

		final JSONObject daily = root.getJSONObject("D").getJSONObject(yyyyMMdd),
				w = root.getJSONObject("W").getJSONObject(weeklyYyyyMMdd),
				m = root.getJSONObject("M").getJSONObject(yyyyMM);
		web3JConfig.sendWarn("赎回统计",
				"### 赎回统计  \n  "
						+ "#### 日期：**" + d + "**  \n  "
						+ "赎回总量：**<font color=\"red\">" + formatAmount(root.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁总量：**" + formatAmount(root.getBigDecimal(totalRecycle)) + " XAI**  \n  "
						+ "#### 当日  \n  "
						+ "赎回：**<font color=\"red\">" + formatAmount(daily.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(daily.getBigDecimal(totalRecycle)) + " XAI**  \n  "
						+ "#### 当周【**" + weeklyYyyyMMdd + "**】  \n  "
						+ "赎回：**<font color=\"red\">" + formatAmount(w.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(w.getBigDecimal(totalRecycle)) + " XAI**  \n  "
						+ "#### 当月  \n  "
						+ "赎回：**<font color=\"red\">" + formatAmount(m.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(m.getBigDecimal(totalRecycle)) + " XAI**  \n  "
		);
	}

	public static String formatAmount(BigDecimal amount) {
		return format.format(amount).replaceAll("\\$", "").replaceAll(".00", "");
	}

	private final static String totalRedemption = "totalRedemption", totalRecycle = "totalRecycle";

	public synchronized void incrStat(BigDecimal redemptionAmount, BigDecimal recycleAmount) throws Exception {
		final JSONObject root = deserialization(web3JConfig.statFilePath);

		root.put(totalRedemption, NumberUtil.getBigDecimal(root.getBigDecimal(totalRedemption), BigDecimal.ZERO).add(redemptionAmount));
		root.put(totalRecycle, NumberUtil.getBigDecimal(root.getBigDecimal(totalRecycle), BigDecimal.ZERO).add(recycleAmount));

		final EasyDate d = new EasyDate();
		final String yyyyMMdd = Formats.getYyyyMMdd(d) + "", yyyyMM = Formats.getYyyyMM(d) + "", weeklyYyyyMMdd = Formats.getYyyyMMdd(d.addDay(1 - d.getWeekDay())) + "";

		// 统计日、周、月 赎回总额
		stat(root, "D", yyyyMMdd, redemptionAmount, recycleAmount);
		stat(root, "W", weeklyYyyyMMdd, redemptionAmount, recycleAmount);
		stat(root, "M", yyyyMM, redemptionAmount, recycleAmount);

		serialization(web3JConfig.statFilePath, root);
	}

	private void stat(JSONObject root, String key, String period, BigDecimal redemptionAmount, BigDecimal recycleAmount) {
		JSONObject dailyStat = root.getJSONObject(key);
		if (dailyStat == null) {
			dailyStat = new JSONObject();
		}
		JSONObject stat = dailyStat.getJSONObject(period);
		if (stat == null) {
			stat = new JSONObject();
		}
		stat.put(totalRedemption, NumberUtil.getBigDecimal(stat.getBigDecimal(totalRedemption), BigDecimal.ZERO).add(redemptionAmount));
		stat.put(totalRecycle, NumberUtil.getBigDecimal(stat.getBigDecimal(totalRecycle), BigDecimal.ZERO).add(recycleAmount));
		dailyStat.put(period, stat);
		root.put(key, dailyStat);
	}

	public static synchronized void serialization(String url, JSONObject data) throws Exception {
		FileOutputStream fos = new FileOutputStream(url);
		ObjectOutputStream os = new ObjectOutputStream(fos);
		os.writeObject(data);
		os.close();
	}

	public static synchronized JSONObject deserialization(String url) throws Exception {
		FileInputStream fis = new FileInputStream(url);
		ObjectInputStream is = new ObjectInputStream(fis);
		JSONObject data = null;
		try {
			data = (JSONObject) is.readObject();
		} catch (ClassNotFoundException ignored) {
		}

		is.close();
		return data;
	}

	public static void main(String[] args) throws Exception {
		// String format = DecimalFormat.getCurrencyInstance(Locale.US).format(new BigDecimal("409244.65"));
		// System.out.println(format);

		final EasyDate d = new EasyDate();
		final String yyyyMMdd = Formats.getYyyyMMdd(d) + "", yyyyMM = Formats.getYyyyMM(d) + "", weeklyYyyyMMdd = Formats.getYyyyMMdd(d.addDay(1 - d.getWeekDay())) + "";

		JSONObject root = deserialization("/mnt/xai_bot/stat.json");
		root.put("totalRedemption", new BigDecimal("838194.65"));
		root.put("totalRecycle", new BigDecimal("1257291.98"));

		root.getJSONObject("D").getJSONObject(yyyyMMdd).put(totalRedemption, BigDecimal.ZERO);
		root.getJSONObject("D").getJSONObject(yyyyMMdd).put(totalRecycle, BigDecimal.ZERO);

		root.getJSONObject("W").getJSONObject(weeklyYyyyMMdd).put(totalRedemption, new BigDecimal("66383"));
		root.getJSONObject("W").getJSONObject(weeklyYyyyMMdd).put(totalRecycle, new BigDecimal("99575"));

		root.getJSONObject("M").getJSONObject(yyyyMM).put(totalRedemption, new BigDecimal("409244.65"));
		root.getJSONObject("M").getJSONObject(yyyyMM).put(totalRecycle, new BigDecimal("613866.98"));

		System.out.println(Jsons.encode(root));

		serialization("/mnt/xai_bot/stat.json", root);
	}

}
