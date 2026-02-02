package com.bojiu.webapp.base.entity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bojiu.common.model.BizFlag;
import com.bojiu.common.model.Status;
import com.bojiu.common.util.Common;
import com.bojiu.common.util.Formats;
import com.bojiu.context.model.*;
import jodd.util.Base32;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Setter
@Getter
public abstract class BaseMember extends BizEntity implements Member {

	protected static final String letterChars = "abcdefghijklmnopqrstuvwxyz", digitChars = "0123456789";
	public static final long BASE_MS = new EasyDate(2026, 1, 1).getTime();

	/** 手机号码已认证 1 */
	public static final int PHONE_VERIFIED = 1;
	/** 邮箱已认证 2 */
	public static final int EMAIL_VERIFIED = 1 << 1;
	/** 已实名认证 4 */
	public static final int REAL_NAME_VERIFIED = 1 << 2;
	/** 兑换密码已设置 8 */
	public static final int PAY_PWD_SET = 1 << 3;
	/** 绑定提现账号 16 */
	public static final int BIND_CASH_ACCOUNT = 1 << 4;
	/** 注册IP一个月内重复 32 */
	public static final int REG_IP_MONTH_REPEAT = 1 << 5;
	/** 登录密码已设置 64 */
	public static final int PWD_SET = 1 << 6;

	/** 商户ID：为 0 表示平台 */
	protected Long merchantId;
	/** 用户名 */
	protected String username;
	/** 昵称 */
	protected String nickname;
	/** 手机号码 */
	protected String phone;
	/** 凭证认证状态（位运算）：1=手机号码已认证；2=邮箱已认证；4=已实名认证；8=兑换密码已设置 */
	protected Integer proofFlag;
	/** 邮箱 */
	protected String email;
	/** 性别：1=男；0=女 */
	protected Gender gender;
	/** 用户头像 */
	protected String avatar;
	/** 最后一次登录时间 */
	protected Date lastLoginTime;
	/** 登录方式 */
	protected Integer loginWay;
	/** 业务标识 */
	protected long bizFlag;

	protected BaseMember init(BaseMember input, Date now) {
		username = X.expectNotEmpty(username, input.username, input.phone, input.email);
		merchantId = X.expectNotNull(merchantId, input.merchantId);
		nickname = X.expectNotNull(input.nickname, nickname, "");
		phone = X.expectNotEmpty(input.phone, phone);
		proofFlag = X.expectNotNull(proofFlag, input.proofFlag, 0);
		email = X.expectNotEmpty(input.email, email);
		gender = X.expectNotNull(input.gender, gender);
		avatar = X.expectNotEmpty(input.avatar, avatar);
		super.initTime(now);
		return this;
	}

	@Override
	public boolean valid() {
		return Status.YES.eq(status);
	}

	@JSONField(deserialize = false)
	public void setGenderValue(Integer value) {
		this.gender = Gender.of(value);
	}

	/** 模糊用户名 */
	public String getUsername_() {
		return Formats.anonymousUsername(getUsername());
	}

	/** 是否已通过手机认证 */
	public boolean isPhoned() {
		return proofFlag != null && BizFlag.hasFlag(proofFlag, PHONE_VERIFIED);
	}

	/** 是否已绑定提现账号 */
	public boolean bindCashAccount() {
		return BizFlag.hasFlag(proofFlag, BIND_CASH_ACCOUNT);
	}

	/** 是否月注册IP不重复 */
	public boolean regIpMonthRepeat() {
		return BizFlag.hasFlag(proofFlag, REG_IP_MONTH_REPEAT);
	}

	/** 是否设置支付密码 */
	public boolean payPwdSet() {
		return BizFlag.hasFlag(proofFlag, PAY_PWD_SET);
	}

	/** 是否设置登录密码 */
	public boolean loginPwdSet() {
		return BizFlag.hasFlag(proofFlag, PWD_SET);
	}

	/** 是否已通过邮箱认证 */
	public boolean isEmailed() {
		return proofFlag != null && BizFlag.hasFlag(proofFlag, EMAIL_VERIFIED);
	}

	public void addEmailStatus(Integer emailStatus) {
		this.proofFlag = (int) BizFlag.WritableBizFlag.toggleFlag(proofFlag, EMAIL_VERIFIED, emailStatus == 1);
	}

	public void addPhoneStatus(Integer phoneStatus) {
		this.proofFlag = (int) BizFlag.WritableBizFlag.toggleFlag(proofFlag, PHONE_VERIFIED, phoneStatus == 1);
	}

	public void addPwdStatus() {
		this.proofFlag = (int) BizFlag.WritableBizFlag.toggleFlag(proofFlag, PWD_SET, true);
	}

	/** 模糊手机号 */
	public String getPhone_() {
		return Formats.anonymousPhone(getPhone());
	}

	static final String PASSWORD_SALT = "C?J+kY9z?o3_dBxT*Wk66i.Ah5ph#y99xk";

	public static String encryptPassword(String username, String password, Date addTime, boolean worker) {
		Assert.notEmpty(username, "username is required");
		Assert.notNull(addTime, "addTime is required");
		final String str = username + '@' + password + '@' + PASSWORD_SALT + '@' + Formats.formatDate_D(addTime);
		if (worker || MemberType.worker()) { // 代打平台 使用 Base64 编码以缩短字符长度
			byte[] encoded = Encrypter.encode(JavaUtil.getUtf8Bytes(str), "SHA-224");
			return Common.base64ToString(encoded, true);
		}
		return Encrypter.encode(str, "SHA-224");
	}

	public static String encryptPassword(String username, String password, Date addTime) {
		return encryptPassword(username, password, addTime, false);
	}

	public static String encryptGoogleSecretKey(String username, Date addTime) {
		Assert.notEmpty(username, "username is required");
		Assert.notNull(addTime, "addTime is required");
		final String str = username + '@' + PASSWORD_SALT + '@' + addTime.getTime();
		// Google身份验证器只识别 Base32 格式秘钥
		return Base32.encode(Encrypter.getMessageDigest("SHA-224").digest(JavaUtil.getUtf8Bytes(str)));
	}

	/** 根据用户ID */
	public static String generateInviteCode(Long memberId) {
		final String code = Long.toString(memberId, 36).toUpperCase();
		return "u" + code + checkDigit(memberId);
	}

	/** 校验位算法 */
	static int checkDigit(Long userId) {
		int h = Long.hashCode(userId);
		return Math.abs((h ^ (h >>> 16)) % 10);
	}

	public static Long parseMemberId(String inviteCode) {
		// 由于前端传进来的参数可能是一个用户ID，也可能是一个邀请码，有可能混淆解析
		// 因此，新生成的邀请码必须以小写字母 "u" 开头，如果传进来的参数不以 "u" 开头，且是纯数字的，则认为是用户ID
		if (inviteCode == null) {
			return null;
		}
		boolean prefix = inviteCode.startsWith("u");
		if (!prefix && (inviteCode.isEmpty() || NumberUtil.isNumber(inviteCode))) {
			return null;
		}
		int pos = inviteCode.lastIndexOf('_');
		if (pos == 1) { // "u_"
			return null;
		}
		inviteCode = inviteCode.substring(prefix ? 1 : 0, pos == -1 ? inviteCode.length() : pos).toLowerCase();
		final int endIndex = inviteCode.length() - 1;
		final long memberId = Long.parseLong(inviteCode, 0, endIndex, 36);
		return memberId > 0 && checkDigit(memberId) == Character.digit(inviteCode.charAt(endIndex), 10) ? memberId : null;
	}

	@Nullable
	public static Long safeParseMemberId(String inviteCode) {
		try {
			return parseMemberId(inviteCode);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	/**
	 * 对 明文密码 进行编码处理
	 *
	 * @param password 原始明文密码
	 */
	public String encryptPassword(String password) {
		return encryptPassword(username, password, addTime);
	}

	/**
	 * 设置密码
	 *
	 * @param password 密码
	 * @param original 是否为原始明文密码，如果为true，则内部会对其进行加密处理
	 */
	public void setPassword(String password, boolean original) {
		if (original) {
			setPassword(encryptPassword(username, password, addTime));
		} else {
			setPassword(password);
		}
	}

	/**
	 * 检测用户输入的登录密码是否有效
	 *
	 * @param inputPassword 用户输入的密码
	 * @param original 是否为原始明文密码，如果为true，则内部会对其进行加密处理
	 */
	public final boolean checkPassword(String inputPassword, boolean original) {
		return checkAnyPassword(getUsername(), inputPassword, getAddTime(), getPassword(), original);
	}

	/**
	 * 检测用户输入的登录密码是否有效
	 *
	 * @param inputPassword 用户输入的密码
	 * @param targetPassword 用于比较的已经过加密处理的密码
	 * @param original 是否为原始明文密码，如果为true，则内部会对其进行加密处理
	 */
	public static boolean checkAnyPassword(String username, String inputPassword, Date addTime, String targetPassword, boolean original) {
		if (StringUtil.isEmpty(inputPassword)) {
			return false;
		}
		if (original) {
			inputPassword = encryptPassword(username, inputPassword, addTime);
		}
		return targetPassword.equals(inputPassword);
	}

	/**
	 * 生成随机个数头像ID列表
	 */
	public static @NonNull List<Integer> batchGenerateRandomAvatars(int size) {
		final int start = 10001, end = 10013;
		final int totalAvailable = end - start;
		// 构造头像ID列表
		final List<Integer> allAvatars = new ArrayList<>(totalAvailable);
		for (int i = start; i < end; i++) {
			allAvatars.add(i);
		}
		// 打乱列表
		Collections.shuffle(allAvatars, ThreadLocalRandom.current());
		if (size <= totalAvailable) {
			return allAvatars.subList(0, size);
		}
		List<Integer> result = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			result.add(allAvatars.get(i % totalAvailable));
		}
		return result;
	}

	/** 生成随机个数的用户名 */
	public static @NonNull List<String> batchGenerateRandomUsernames(int size, int maxLength) {
		final Random random = ThreadLocalRandom.current();
		final List<String> names = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			names.add(generateUsername(random, 4, maxLength, false));
		}
		return names;
	}

	/**
	 * 生成指定个数的随机用户名
	 *
	 * @see #generateUsername(int, int, boolean)
	 */
	public static List<String> batchGenerateRandomUsernames(int size) {
		return batchGenerateRandomUsernames(size, 8);
	}

	public static final String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	/** 随机生成密码 */
	public static String generatePwd(final Random random, int minLength, int maxLength) {
		if (minLength < 4) {
			throw new IllegalArgumentException();
		}
		final byte[] charPool = JavaUtil.STRING_VALUE.apply(CHAR_POOL);
		final byte firstChar = charPool[random.nextInt(letterChars.length())];
		final int poolLength = charPool.length;
		final int charNum = minLength == maxLength ? minLength : random.nextInt(minLength, maxLength + 1);
		final byte[] chars = new byte[charNum];
		chars[0] = firstChar;
		for (int i = 1; i < charNum; i++) {
			chars[i] = charPool[random.nextInt(poolLength)];
		}
		return JavaUtil.STRING_CREATOR_JDK11.apply(chars, JavaUtil.LATIN1);
	}

	/**
	 * 随机生成用户名 规则如下：
	 * <p> 1、以随机字母开头
	 * <p> 2、<code> [ minLength, maxLength ] </code> 个随机字母/数字
	 *
	 * @param anonymous 是否模糊化处理，如 "12***67"
	 */
	public static String generateUsername(final Random random, int minLength, int maxLength, boolean anonymous) {
		if (minLength < 4) {
			throw new IllegalArgumentException();
		}
		final byte[] charPool = JavaUtil.STRING_VALUE.apply(letterChars + digitChars);
		final byte firstChar = charPool[random.nextInt(letterChars.length())];
		final int poolLength = charPool.length;
		final byte[] chars;
		if (anonymous) { // 模糊化形式为 "12***67"
			chars = new byte[] { firstChar, charPool[random.nextInt(poolLength)],
					(byte) '*', (byte) '*', (byte) '*',
					charPool[random.nextInt(poolLength)], charPool[random.nextInt(poolLength)]
			};
		} else {
			final int charNum = minLength == maxLength ? minLength : random.nextInt(minLength, maxLength + 1);
			chars = new byte[charNum];
			chars[0] = firstChar;
			for (int i = 1; i < charNum; i++) {
				chars[i] = charPool[random.nextInt(poolLength)];
			}
		}
		return JavaUtil.STRING_CREATOR_JDK11.apply(chars, JavaUtil.LATIN1);
	}

	/**
	 * 随机生成用户名 规则如下：
	 * <p> 1、以随机字母开头
	 * <p> 2、<code> [ minLength, maxLength ] </code> 个随机字母/数字
	 *
	 * @param anonymous 是否模糊化处理，如 "12***67"
	 */
	public static String generateUsername(int minLength, int maxLength, boolean anonymous) {
		return generateUsername(ThreadLocalRandom.current(), minLength, maxLength, anonymous);
	}

	/**
	 * 随机生成用户名 规则如下：
	 * <p> 1、以随机字母开头
	 * <p> 2、随机 4~16 个字母/数字
	 *
	 * @param anonymous 是否模糊化处理，如 "12***67"
	 */
	public static String generateUsername(final Random random, boolean anonymous) {
		return generateUsername(random, 4, 16, anonymous);
	}

	/**
	 * 随机生成用户名 规则如下：
	 * <p> 1、以随机字母开头
	 * <p> 2、随机 4~16 个字母/数字
	 *
	 * @param anonymous 是否模糊化处理，如 "12***67"
	 */
	public static String generateUsername(boolean anonymous) {
		return generateUsername(ThreadLocalRandom.current(), anonymous);
	}

	public static @NonNull List<Pair<Integer, String>> generateRobot(int size) {
		List<Pair<Integer, String>> list = new ArrayList<>(size);
		final List<String> usernames = batchGenerateRandomUsernames(size, 13);
		final List<Integer> avatars = batchGenerateRandomAvatars(size);
		int i = 0;
		for (Integer integer : avatars) {
			list.add(Pair.of(integer, usernames.get(i++)));
		}
		return list;
	}

	/**
	 * 随机生成用户名
	 */
	public static String generateUsername() {
		long l = (System.currentTimeMillis() - BASE_MS) * 1000 + RandomUtil.nextInt(0, 999);
		return Member.idPrefix() + Long.toString(l, 36);
	}

	public static final String USERNAME = "username";
	public static final String PHONE = "phone";
	public static final String TYPE = "type";
	public static final String EMAIL = "email";
	public static final String NICKNAME = "nickname";
	public static final String GENDER = "gender";
	public static final String AVATAR = "avatar";
	public static final String PROOF_FLAG = "proof_flag";
	public static final String LAST_LOGIN_TIME = "last_login_time";
	public static final String LOGIN_WAY = "login_way";
	public static final String BIZ_FLAG = "biz_flag";

}