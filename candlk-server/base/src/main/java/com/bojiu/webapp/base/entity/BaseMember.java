package com.bojiu.webapp.base.entity;

import java.util.Date;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bojiu.common.model.BizFlag;
import com.bojiu.common.model.Status;
import com.bojiu.common.util.Formats;
import com.bojiu.context.model.Gender;
import com.bojiu.context.model.Member;
import jodd.util.Base32;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public abstract class BaseMember extends BizEntity implements Member {

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

	public void setGender(Gender gender) {
		this.gender = gender;
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

	public static String encryptPassword(String username, String password, Date addTime) {
		Assert.notEmpty(username, "username is required");
		Assert.notNull(addTime, "addTime is required");
		final String str = username + '@' + password + '@' + PASSWORD_SALT + '@' + Formats.formatDate_D(addTime);
		return Encrypter.encode(str, "SHA-224");
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
		return code + checkDigit(memberId);
	}

	/** 校验位算法 */
	static int checkDigit(Long userId) {
		int h = Long.hashCode(userId);
		return Math.abs((h ^ (h >>> 16)) % 10);
	}

	public static Long parseMemberId(String inviteCode) {
		inviteCode = StringUtils.substringBefore(inviteCode, "_");
		if (StringUtil.isEmpty(inviteCode)) {
			return null;
		}
		inviteCode = inviteCode.toLowerCase();
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

	public static final String STATUS_LOGIN_ERROR = "invalid";

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