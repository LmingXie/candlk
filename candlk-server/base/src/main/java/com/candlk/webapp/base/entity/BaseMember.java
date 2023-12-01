package com.candlk.webapp.base.entity;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.alibaba.fastjson2.annotation.JSONField;
import com.candlk.common.model.BizFlag;
import com.candlk.common.model.Status;
import com.candlk.common.util.Formats;
import com.candlk.context.model.Gender;
import com.candlk.context.model.Member;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.*;

@Setter
@Getter
public abstract class BaseMember extends BizEntity implements Member, BizFlag.WritableBizFlag {

	/** 用户名 */
	protected String username;
	/** 密码 */
	protected String password;
	/** 商户ID：为 0 表示平台 */
	protected Long merchantId;
	/** 昵称 */
	protected String nickname;
	/** 手机号码 */
	protected String phone;
	/** 手机认证状态：1=已认证；0=未认证 */
	protected Integer phoneStatus;
	/** 邮箱 */
	protected String email;
	/** 邮箱认证状态：1=已认证；0=未认证 */
	protected Integer emailStatus;
	/** 生日 */
	protected Date birthday;
	/** 性别：1=男；0=女 */
	protected Gender gender;
	/** 用户头像 */
	protected String avatar;
	/** 业务标识 */
	protected long bizFlag;
	/** 最后一次登录时间 */
	protected Date lastLoginTime;
	/** 最后一次登录IP */
	protected String lastLoginIp;

	protected BaseMember init(BaseMember input, Date now) {
		username = X.expectNotEmpty(input.username, username, input.phone, input.email);
		password = X.expectNotNull(password, input.password);
		merchantId = X.expectNotNull(merchantId, input.merchantId);
		nickname = X.expectNotNull(input.nickname, nickname, "");
		phone = X.expectNotEmpty(input.phone, phone);
		phoneStatus = X.expectNotNull(phoneStatus, input.phoneStatus, Status.NO.value);
		email = X.expectNotEmpty(email, input.email);
		emailStatus = X.expectNotNull(emailStatus, input.phoneStatus, Status.NO.value);
		birthday = X.expectNotNull(birthday, input.birthday);
		gender = X.expectNotNull(input.gender, gender);
		avatar = X.expectNotEmpty(input.avatar, avatar);
		super.initTime(now);
		return this;
	}

	@Override
	public boolean isValid() {
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

	/** 模糊手机号 */
	public String getPhone_() {
		return Formats.anonymousPhone(getPhone());
	}

	@Override
	public void setBizFlag(long flag) {
		this.bizFlag = flag;
	}

	static final String PASSWORD_SALT = "ZdBI.Pz%zE—1J@NYfWF_5LkR/lH#0rBbaOw=";

	public static String encryptPassword(String username, String password, Date addTime) {
		Assert.notEmpty(username, "username is required");
		Assert.notNull(addTime, "addTime is required");
		final String str = username + '@' + password + '@' + PASSWORD_SALT + '@' + Formats.formatDate_D(addTime);
		return Encrypter.bytes2Hex(Encrypter.getMessageDigest("SHA-224").digest(str.getBytes(StandardCharsets.UTF_8)));
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
		if (StringUtil.isEmpty(inviteCode)) {
			return null;
		}
		inviteCode = inviteCode.toLowerCase();
		final int endIndex = inviteCode.length() - 1;
		final String encoded = inviteCode.substring(0, endIndex);
		final long memberId = Long.parseLong(encoded, 36);
		return memberId > 0 && checkDigit(memberId) == Character.digit(inviteCode.charAt(endIndex), 10) ? memberId : null;
	}

	/**
	 * 设置密码
	 *
	 * @param password 密码
	 * @param original 是否为原始明文密码，如果为true，则内部会对其进行加密处理
	 */
	public void setPassword(String password, boolean original) {
		if (original) {
			password = encryptPassword(username, password, addTime);
		}
		this.password = password;
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

	public Integer age() {
		final Date birthday = getBirthday();
		return birthday == null ? null : Formats.getAge(birthday);
	}

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String PHONE = "phone";
	public static final String TYPE = "type";
	public static final String EMAIL = "email";
	public static final String NICKNAME = "nickname";
	public static final String BIRTHDAY = "birthday";
	public static final String GENDER = "gender";
	public static final String AVATAR = "avatar";
	public static final String DEVICE_ID = "device_id";
	public static final String BIZ_FLAG = "biz_flag";
	public static final String CLIENT = "client";
	public static final String EMAIL_STATUS = "email_status";
	public static final String PHONE_STATUS = "phone_status";
	public static final String LAST_LOGIN_TIME = "last_login_time";

}
