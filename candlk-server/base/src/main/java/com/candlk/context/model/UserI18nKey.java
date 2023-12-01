package com.candlk.context.model;

public interface UserI18nKey extends BaseI18nKey {

	String INVALID_USER = "invalid_user";
	/** 手机号码格式不正确，请重新输入！ */
	String PHONE_FORMAT_ERROR = "phone.format.error";
	/** 该账户异常，无法进行相关操作！ */
	String USER_AUTO_LOGIN_FROZEN = "user.auto.login.frozen";
	/** 您尚未登录或登录已失效，请重新登录！ */
	String USER_AUTO_LOGIN_INVALID = "user.auto.login.invalid";
	/** 验证码不正确，请重新获取验证码！ */
	String CAPTCHA_ERROR_REDO = "captcha.error.redo";
	/** 验证码不正确，请重新输入！ */
	String CAPTCHA_ERROR = "captcha.error";
	/** 您输入的数据有误，请重新输入！ */
	String INPUT_ERROR = "input.error";
	/** 密码格式不正确！ */
	String PWD_FORMAT_ERROR = "pwd.format.error";
	/** 新登录密码不能与当前登录密码相同！ */
	String CHANGE_PWD_SAME = "change.pwd.same";
	/** 密码不正确，请重新输入！ */
	String PWD_ERROR = "密码不正确，请重新输入！";
	/** 登录失败，账号已注销！ */
	String USER_CLOSED = "user.closed";
	/** 该用户不存在！ */
	String USER_404 = "该用户不存在！";
	/** 检测到账户异常，登录失败，如有疑问请联系客服！ */
	String USER_FROZEN = "user.frozen";

	/** 无法识别的文件业务类型：{0}！ */
	String FILE_BIZ_TYPE_UNKNOWN = "file.biz.type.unknown";
	/** 奖励已领取 */
	String USER_REWARD_RECEIVED = "user.reward.received";
	/** 每日抽取次数超限 */
	String DAILY_TURNTABLE_OVERRUN = "daily.turntable.overrun";
	/** 生日彩蛋未到领取时间 */
	String BIRTHDAY_NOT_ARRIVED = "birthday.not.arrived";
	/** 领取生日彩蛋前需要设置用户生日 */
	String BIRTHDAY_NOT_SET = "birthday.not.set";
	/** 奖励未发放 */
	String REWARD_NOT_GRANT = "reward.not.grant";
	/** 兑换密码不正确 */
	String EXCHANGE_PWD_ERROR = "exchange.pwd.error！";
	/** 不允许输入赠送金额 */
	String GIFT_AMOUNT_NOT_INPUT = "git.amount.not.input";
	/** 优惠活动已关闭 */
	String PROMOTION_CLOSED = "promotion.closed";
	/** 充值优惠活动固定奖励描述 */
	String PROMOTION_RECHARGE_FIX_REWARD = "promotion.recharge.fix.reward";
	/** 充值优惠活动随机奖励描述 */
	String PROMOTION_RECHARGE_RANDOM_REWARD = "promotion.recharge.random.reward";
	/** 充值优惠活动按比例奖励描述 */
	String PROMOTION_RECHARGE_RATIO_REWARD = "promotion.recharge.ratio.reward";
	/** 打码优惠活动固定奖励描述 */
	String PROMOTION_PLAY_FIX_REWARD = "promotion.play.fix.reward";
}
