package com.bojiu.webapp.user.utils;

/**
 * HG赔率转换器
 */
public class HGOddsConverter {

	/**
	 * 转换赔率（不包含本金）
	 * <p>源码：_self.getOBT</p>
	 */
	public static double[] convertOddsRatio(double iorH, double iorC, int configIor, String odd) {
		if ("HK".equals(odd)) {
			odd = "H";
		}
		final String tmpOdd = odd != null ? odd : "H"; // 默认处理，你可按需求替换 userData

		if (iorH == 0 && iorC == 0) {
			return new double[] { 0, 0 };
		}
		return getOtherIoRatio(tmpOdd, iorH, iorC, 100/*参考源码 chg_showior 函数*/, configIor);
	}

	private static double[] getOtherIoRatio(String oddType, double iorH, double iorC, int showIor, int iorPoints) {
		if (iorH > 0 || iorC > 0) {
			return chgIor(oddType, iorH, iorC, showIor, iorPoints);
		}
		return new double[] { iorH, iorC };
	}

	private static double[] chgIor(String oddF, double iorH, double iorC, int showIor, int iorPoints) {
		iorH = Math.floor(iorH * 1000 + 0.001) / 1000;
		iorC = Math.floor(iorC * 1000 + 0.001) / 1000;

		if (iorH < 11) {
			iorH *= 1000;
		}
		if (iorC < 11) {
			iorC *= 1000;
		}

		double[] ior = switch (oddF) {
			case "H" -> getHKIor(iorH, iorC);
			case "M" -> getMAIor(iorH, iorC);
			case "I" -> getINDIorNew(iorH, iorC);
			case "E" -> getEUIor(iorH, iorC);
			default -> new double[] { iorH, iorC };
		};

		ior[0] /= 1000;
		ior[1] /= 1000;

		ior[0] = printf(decimalPoint(ior[0], showIor), iorPoints);
		ior[1] = printf(decimalPoint(ior[1], showIor), iorPoints);

		return ior;
	}

	private static double[] getHKIor(double H_ratio, double C_ratio) {
		double[] outIor = new double[2];
		double line, lowRatio, nowRatio, highRatio;
		String nowType;

		if (H_ratio <= 1000 && C_ratio <= 1000) {
			outIor[0] = Math.floor(H_ratio / 10 + 1e-4) * 10;
			outIor[1] = Math.floor(C_ratio / 10 + 1e-4) * 10;
			return outIor;
		}

		line = 2000 - (H_ratio + C_ratio);
		if (H_ratio > C_ratio) {
			lowRatio = C_ratio;
			nowType = "C";
		} else {
			lowRatio = H_ratio;
			nowType = "H";
		}

		if (2000 - line - lowRatio > 1000) {
			nowRatio = (lowRatio + line) * -1;
		} else {
			nowRatio = 2000 - line - lowRatio;
		}

		if (nowRatio < 0) {
			highRatio = Math.floor(Math.abs(1000 / nowRatio) * 1000);
		} else {
			highRatio = 2000 - line - nowRatio;
		}

		if (nowType.equals("H")) {
			outIor[0] = Math.floor(lowRatio / 10 + 1e-4) * 10;
			outIor[1] = Math.floor(highRatio / 10 + 1e-4) * 10;
		} else {
			outIor[0] = Math.floor(highRatio / 10 + 1e-4) * 10;
			outIor[1] = Math.floor(lowRatio / 10 + 1e-4) * 10;
		}
		return outIor;
	}

	private static double[] getMAIor(double H_ratio, double C_ratio) {
		double[] outIor = new double[2];
		double line, lowRatio, highRatio;
		String nowType;

		if (H_ratio <= 1000 && C_ratio <= 1000) {
			outIor[0] = H_ratio;
			outIor[1] = C_ratio;
			return outIor;
		}

		line = 2000 - (H_ratio + C_ratio);

		if (H_ratio > C_ratio) {
			lowRatio = C_ratio;
			nowType = "C";
		} else {
			lowRatio = H_ratio;
			nowType = "H";
		}

		highRatio = (lowRatio + line) * -1;

		if (nowType.equals("H")) {
			outIor[0] = lowRatio;
			outIor[1] = highRatio;
		} else {
			outIor[0] = highRatio;
			outIor[1] = lowRatio;
		}
		return outIor;
	}

	private static double[] getINDIorNew(double H_ratio, double C_ratio) {

		String Hs = String.valueOf((int) H_ratio);
		String Cs = String.valueOf((int) C_ratio);

		H_ratio = Double.parseDouble(Hs.substring(0, Hs.length() - 1) + "0");
		C_ratio = Double.parseDouble(Cs.substring(0, Cs.length() - 1) + "0");

		double[] outIor = getMAIor(H_ratio, C_ratio);

		H_ratio = outIor[0] / 1000;
		C_ratio = outIor[1] / 1000;

		if (H_ratio != 1) {
			H_ratio = Math.floor(-1 / H_ratio * 100 + 1e-4) / 100;
		}

		if (C_ratio != 1) {
			C_ratio = Math.floor(-1 / C_ratio * 100 + 1e-4) / 100;
		}

		return new double[] { H_ratio * 1000, C_ratio * 1000 };
	}

	private static double[] getEUIor(double H_ratio, double C_ratio) {
		double[] out = getHKIor(H_ratio, C_ratio);
		out[0] += 1000;
		out[1] += 1000;
		return out;
	}

	/** =============== 工具函数 =============== */

	private static double decimalPoint(double val, int showIor) {
		return Math.floor(val * showIor + 1e-4) / showIor;
	}

	private static double printf(double val, int digits) {
		final double factor = Math.pow(10, digits);
		return Math.floor(val * factor + 1e-4) / factor;
	}

}
