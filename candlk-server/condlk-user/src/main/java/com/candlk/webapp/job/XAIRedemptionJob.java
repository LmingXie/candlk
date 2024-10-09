package com.candlk.webapp.job;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.util.Formats;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.NumberUtil;
import org.apache.commons.io.FileUtils;
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
		final String yyyyMMdd = Formats.getYyyyMMdd(d) + "", yyyyMM = Formats.getYyyyMM(d) + "", outM = d.getYear() + "-" + d.getMonth(),
				weeklyYyyyMMdd = Formats.getYyyyMMdd(d.addDay(1 - d.getWeekDay())) + "", outW = d.getYear() + "-" + d.getMonth() + "-" + d.getDay();
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
						+ "赎回：**<font color=\"red\">" + formatAmount(daily == null ? BigDecimal.ZERO : daily.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(daily == null ? BigDecimal.ZERO : daily.getBigDecimal(totalRecycle)) + " XAI**  \n  "
						+ "#### 当周【**" + outW + "**】  \n  "
						+ "赎回：**<font color=\"red\">" + formatAmount(w == null ? BigDecimal.ZERO : w.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(w == null ? BigDecimal.ZERO : w.getBigDecimal(totalRecycle)) + " XAI**  \n  "
						+ "#### 当月【**" + outM + "**】  \n  "
						+ "赎回：**<font color=\"red\">" + formatAmount(m == null ? BigDecimal.ZERO : m.getBigDecimal(totalRedemption)) + " XAI</font>**  \n  "
						+ "销毁：**" + formatAmount(m == null ? BigDecimal.ZERO : m.getBigDecimal(totalRecycle)) + " XAI**  \n  "
				,
				"*Redemption Stat*\n" +
						"Time: *" + d + "* \n" +
						"\n*ToDay: * \n" +
						"Redemption: *" + formatAmount(daily == null ? BigDecimal.ZERO : daily.getBigDecimal(totalRedemption)) + " XAI*\n" +
						"Burn❤\uFE0F\u200D\uD83D\uDD25：" + formatAmount(daily == null ? BigDecimal.ZERO : daily.getBigDecimal(totalRecycle)) + " XAI\n" +

						"\n*Week(" + outW + "): * \n" +
						"Redemption: *" + formatAmount(w == null ? BigDecimal.ZERO : w.getBigDecimal(totalRedemption)) + " XAI*\n" +
						"Burn❤\uFE0F\u200D\uD83D\uDD25：" + formatAmount(w == null ? BigDecimal.ZERO : w.getBigDecimal(totalRecycle)) + " XAI\n" +

						"\n*Month(" + outM + "): * \n" +
						"Redemption: *" + formatAmount(m == null ? BigDecimal.ZERO : m.getBigDecimal(totalRedemption)) + " XAI*\n" +
						"Burn❤\uFE0F\u200D\uD83D\uDD25：" + formatAmount(m == null ? BigDecimal.ZERO : m.getBigDecimal(totalRecycle)) + " XAI\n" +

						"\n*History Total: * \n" +
						"Redemption: *" + formatAmount(root.getBigDecimal(totalRedemption)) + " XAI*\n" +
						"Burn❤\uFE0F\u200D\uD83D\uDD25：*" + formatAmount(root.getBigDecimal(totalRecycle)) + " XAI*\n");
	}

	public static String formatAmount(BigDecimal amount) {
		return format.format(amount).replaceAll("\\$", "").replaceAll("\\.00", "");
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

	// 持久化数据为 JSON 文件
	public static synchronized void serialization(String url, JSONObject data) throws Exception {
		FileUtils.writeStringToFile(new File(url), data.toJSONString(), StandardCharsets.UTF_8);
	}

	// 读取 JSON 文件并反序列化为 JSONObject
	public static synchronized JSONObject deserialization(String path) throws Exception {
		return JSONObject.parseObject(FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8));
	}

}
