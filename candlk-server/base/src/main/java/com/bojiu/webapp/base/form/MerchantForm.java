package com.bojiu.webapp.base.form;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.TimeInterval;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.*;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.entity.Merchant;
import lombok.*;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;

@Getter
@Setter
public class MerchantForm<E> extends BaseForm<E> {

	/** 商户ID/站点ID */
	public Long merchantId;
	/** 商户ID集合 */
	public Collection<Long> merchantIds;
	/** 用户ID */
	public Long userId;
	/** 用户ID */
	public List<Long> userIdList;
	/** 用户账号 */
	public String username;
	/** 顶层代理用户ID */
	@Setter(AccessLevel.NONE)
	public Long topUserId;
	/** 经销商用户ID */
	public Long dealerId;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	public TimeInterval interval;
	/** 国家 */
	public Country country;

	public Long initMerchantId(ProxyRequest q) {
		return initMerchantId(q, false, false);
	}

	public Long initMerchantId(ProxyRequest q, boolean asAgent) {
		return initMerchantId(q, asAgent, false);
	}

	public Long initMerchantId(ProxyRequest q, boolean asAgent, boolean asDealer) {
		final Long mid = q.applyMerchantId(this::setMerchantId);
		if ((asAgent || asDealer) && !Merchant.isPlatform(mid)) {
			final Member sessionUser = q.getSessionUser();
			if (sessionUser != null) {
				if (sessionUser.type() == MemberType.AGENT) {
					applyTopUserId(sessionUser.getTopUserId());
				}
				if (sessionUser.type() == MemberType.DEALER) {
					applyDealerId(sessionUser.getDealerId());
				}
			}
		}
		return mid;
	}

	public void applyTopUserId(Long topUserId) {
		if (topUserId != null) {
			this.topUserId = topUserId;
		}
	}

	public void applyDealerId(Long dealerId) {
		if (dealerId != null) {
			setDealerId(dealerId);
		}
	}

	public boolean isAgent() {
		return topUserId != null;
	}

	public void applyMerchants(Member sessionUser, Long merchantId) {
		this.merchantId = merchantId;
		applyMerchants(sessionUser, sessionUser.merchantIds());
	}

	public void applyMerchants(Member sessionUser) {
		applyMerchants(sessionUser, sessionUser.merchantIds());
	}

	public void applyMerchantsClear(Member sessionUser) {
		applyMerchants(sessionUser, sessionUser.merchantIds());
		merchantId = null;
	}

	public void applyMerchants(Member sessionUser, @Nonnull Collection<Long> merchantIdList) {
		if (sessionUser.asEmp()) {
			if (merchantId != null) {
				initMerchantIds(merchantId);
			} else if (X.isValid(merchantIds)) {
				setMerchantIds(CollectionUtil.filter(merchantIds, merchantIdList::contains));
				I18N.assertTrue(X.isValid(merchantIds), AdminI18nKey.UNSUPPORTED_OPERATIONS);
			} else if (!sessionUser.asAdmin()) {
				initMerchantIds(merchantIdList);
			}
		}
	}

	public void applyMerchantIds(Long siteId, @Nonnull Collection<Long> merchantIdList) {
		if (siteId == null) {
			initMerchantIds(merchantIdList);
		} else {
			initMerchantIds(siteId);
		}
	}

	private transient Long[] merchantIdsArray;

	public void setMerchantIds(List<Long> merchantIds) {
		this.merchantIds = merchantIds;
		this.merchantIdsArray = null;
	}

	public void initMerchantIds(Collection<Long> merchantIds) {
		this.merchantIds = merchantIds;
		this.merchantIdsArray = null;
	}

	public void initMerchantIds(Long... merchantIds) {
		this.merchantIds = Arrays.asList(merchantIds);
		this.merchantIdsArray = merchantIds;
	}

	public void initMerchantIds(Long merchantId) {
		this.merchantIds = Collections.singletonList(merchantId);
	}

	public Long firstOfMerchantIds() {
		return CollectionUtil.getAny(merchantIds);
	}

	@Nullable
	public Long[] toMerchantIds() {
		if (merchantIdsArray == null && X.isValid(merchantIds)) {
			return merchantIdsArray = merchantIds.toArray(Long[]::new);
		}
		return merchantIdsArray;
	}

}