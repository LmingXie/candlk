package com.bojiu.webapp.user.entity;

import java.math.BigDecimal;
import java.util.*;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bojiu.common.context.Env;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Bean;
import com.bojiu.common.model.BizFlag;
import com.bojiu.context.auth.PermissionException;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.model.WithMerchant;
import com.bojiu.webapp.base.entity.BaseMember;
import com.bojiu.webapp.base.entity.Merchant;
import lombok.Getter;
import lombok.Setter;

import static com.bojiu.context.auth.Permission.*;

/**
 * 平台&商户 员工表
 *
 * @author LeeYd
 * @since 2023-08-31
 */
@Setter
@Getter
public class Emp extends BaseMember implements BizFlag.WritableBizFlag {

	/** 密码 */
	String password;
	/** 生日 */
	Date birthday;
	/** 最后一次登录IP */
	String lastLoginIp;
	/** 加款单笔限额 */
	BigDecimal rechargeAmountMax;
	/** 出款单笔限额 */
	BigDecimal cashAmountMax;
	/** 所属商户ID */
	Long groupId;
	/** 管理站点范畴【0=所有，其他站点逗号隔开】 */
	String merchantScope;
	/** sessionId */
	String sessionId;
	/** 最顶级代理ID */
	Long topUserId;
	/** 经销商ID */
	Long dealerId;
	/** 员工地址 */
	String addr;
	/** USDT钱包 */
	String walletAddr;
	//
	/** 访客商户角色ID【平台访问商户】 */
	Long visitRoleId;
	/** 访客商户ID【平台登录商户的商户ID】 */
	@TableField(exist = false)
	protected Long visitorMId;
	/** 访客商户ID【平台登录商户的商户ID】 */
	@TableField(exist = false)
	protected Long visitorGId;
	//
	/** 权限码 Set 集合 */
	@TableField(exist = false)
	protected Set<Long> roleIds;
	/** 管理站点ID */
	@TableField(exist = false)
	protected Set<Long> merchantIds;

	protected transient Set<String> permissions;

	@Override
	public Long getMerchantId() {
		return asVisitor() ? visitorMId : merchantId;
	}

	public Long getRealMerchantId() {
		return merchantId;
	}

	public Long getGroupId() {
		return asVisitor() ? visitorGId : groupId;
	}

	public Emp init(Long merchantId, Long groupId, String merchantScope, Long topUserId, String username, long bizFlag, Date now) {
		this.merchantId = merchantId;
		this.groupId = groupId;
		this.merchantScope = merchantScope;
		this.topUserId = topUserId;
		this.username = username;
		this.nickname = username;
		this.bizFlag = bizFlag;
		this.initActiveStatus();
		super.initTime(now);
		return this;
	}

	@Override
	public final boolean asEmp() {
		return true;
	}

	@Override
	public boolean canAccess(Long merchantId) {
		return asAdmin() || merchantIds().contains(merchantId);
	}

	/** 是否属于平台 */
	@Override
	public boolean asAdmin() {
		return Merchant.isPlatform(merchantId);
	}

	/** 是否可作为商户访客 */
	@Override
	public boolean asVisitor() {
		// return visitorMId != null && asAdmin() && Role.hasVisitorRole(visitRoleId) && visitorMId > Merchant.PLATFORM_ID;
		return false;
	}

	/** 是否可进入商户后台 */
	@Override
	public boolean intoMerchant() {
		return asMerchant() || asVisitor();
	}

	/** 是否属于商户 */
	public boolean asMerchant() {
		return !asAdmin();
	}

	/** 是否商户站点 */
	public boolean asSite() {
		return getGroupId() > 0 && !Objects.equals(getMerchantId(), getGroupId());
	}

	/** 是否总商户 */
	@Override
	public boolean asGroup() {
		return getGroupId() > 0 && Objects.equals(getMerchantId(), getGroupId());
	}

	/** 是否具备平台管理员权限 */
	public boolean asSuperAdmin() {
		return hasFlag(EmpBizFlag.PLATFORM_SA);
	}

	/** 是否具备商户管理员权限 */
	public boolean asSuperMerchant() {
		return hasFlag(EmpBizFlag.MERCHANT_SA);
	}

	public boolean asSuperGroupMerchant() {
		return asSuperMerchant() && asGroup();
	}

	public boolean asAgent() {
		return Bean.isValidId(topUserId);
	}

	@Override
	public boolean hasPermission(String code) {
		if (asSuperAdmin()) { // 具备ID=1的用户的所有权限
			return true;
		}
		if (asSuperMerchant() && Env.inLocal()) {
			return true;
		}
		return switch (code) {
			case USER, EMP -> true;
			case MERCHANT, AGENT, DEALER -> intoMerchant();
			case ADMIN -> asAdmin();
			default -> false;/*{
				final Set<String> permissions = getPermissions();
				yield permissions != null && permissions.contains(RoleMenu.simplyPermissionCode(code));
			}*/
		};
	}

	public void checkRechargeAmountMax(BigDecimal amount) {
		if (rechargeAmountMax != null && amount != null) {
			I18N.assertTrue(amount.compareTo(rechargeAmountMax) <= 0, AdminI18nKey.SINGLE_ADD_LIMIT, rechargeAmountMax);
		}
	}

	@Override
	public Set<Long> merchantIds() {
		// if (merchantIds == null && intoMerchant()) {
		// 	merchantIds = ArrayUtil.ins(type(), MemberType.GROUP, MemberType.VISITOR) ? SpringUtil.getBean(MerchantService.class).findIdsByGroup(getMerchantId())
		// 			: Common.splitAsLongSet(merchantScope);
		// }
		return merchantIds;
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	public void assertCanEdit(Long oldMerchantId, Long newMerchantId) {
		if (!oldMerchantId.equals(newMerchantId) || !canAccess(oldMerchantId)) {
			throw new PermissionException();
		}
	}

	/**
	 * 检查指定商户ID的用户是否有权编辑指定商户归属的对象（平台可以访问商户），否则报错
	 */
	public void assertCanEdit(WithMerchant old, WithMerchant input) {
		assertCanEdit(old.getMerchantId(), input.getMerchantId());
	}

	@Override
	public MemberType type() {
		if (asVisitor()) {
			return MemberType.VISITOR;
		} else if (asAdmin()) {
			return MemberType.ADMIN;
		} else if ("0".equals(merchantScope)) {
			return MemberType.GROUP;
		} else if (asAgent()) {
			return MemberType.AGENT;
		}
		return MemberType.MERCHANT;
	}

	@Getter
	public enum EmpBizFlag implements BizFlag {
		/** 1 超管（创始人）id = 1 或 商户超管 */
		SUPER_EMP,
		/** 2 具备平台管理员权限 */
		PLATFORM_SA,
		/** 4 具备商户管理员权限 */
		MERCHANT_SA,
		/** 8 Google身份验证器验证 */
		GOOGLE_AUTH,
		//
		;
		final long bizFlag;

		EmpBizFlag() {
			this.bizFlag = 1L << ordinal();
		}

	}

	public static final String LAST_LOGIN_IP = "last_login_ip";
	public static final String PASSWORD = "password";
	public static final String RECHARGE_AMOUNT_MAX = "recharge_amount_max";
	public static final String CASH_AMOUNT_MAX = "cash_amount_max";
	public static final String MERCHANT_SCOPE = "merchant_scope";
	public static final String SESSION_ID = "session_id";
	public static final String TOP_USER_ID = "top_user_id";
	public static final String DEALER_ID = "dealer_id";
	public static final String ADDR = "addr";
	public static final String WALLET_ADDR = "wallet_addr";
	public static final String VISIT_ROLE_ID = "visit_role_id";

}