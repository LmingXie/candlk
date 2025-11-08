package com.bojiu.context.brand;

import javax.annotation.Nullable;

import static com.bojiu.context.brand.FeatureConfigHandler.*;

/**
 * 功能 & 新特性（二级功能）
 */
@SuppressWarnings("JavadocReference")
public enum Feature {

	/**
	 * 后台皮肤
	 * <p> 存储数据结构：由前端自定义
	 *
	 * @see com.bojiu.webapp.user.model.MetaType#attach_config
	 * @see com.bojiu.webapp.user.dto.AttachConfig#getSkins()
	 */
	BgSkin("后台皮肤", Module.BG_SKIN, 1, 1, forFront()),
	/*
	 * 语言
	 * <p> 存储数据结构：<code> ["1", "2", "3"] </code>
	 *
	 * @see com.bojiu.context.model.Language#getValue()
	 * @see com.bojiu.webapp.base.entity.Merchant#getLanguages()
	 * @see com.bojiu.webapp.base.dto.MerchantContext#getLanguages()
	 */
	// Language("语言", Module.LANGUAGE, 1, 0, forMultiValues(com.bojiu.context.model.Language.CACHE)),
	/*
	 * 货币
	 *
	 * @see com.bojiu.context.model.Currency
	 * @see com.bojiu.webapp.base.entity.Group#getCurrency()
	 * @see com.bojiu.webapp.base.entity.Merchant#getCurrency()
	 * @see com.bojiu.webapp.base.dto.MerchantContext#getCurrency()
	 */
	// Currency("货币", Module.LANGUAGE, 1, 0, null),
	/**
	 * 厂商游戏：< 货币 x 厂商 x 游戏类型 , 分成比例 >
	 * <p>
	 * 存储数据结构：<pre><code>
	 *  {
	 *    "BRL": {
	 *      "123": { // vendorId
	 *        "alias": "PP",
	 *        "type": 1,
	 *        "shareRate": 8
	 *     },
	 * 	 "124": {
	 *        "alias": "PP",
	 *        "type": 2,
	 *        "shareRate": 10
	 *      }
	 *    },
	 *    "playShare":{
	 * 	    "0": {
	 * 		    "shareRate":12
	 *      }
	 *      }
	 *  }
	 * </code></pre>
	 *
	 * @see com.bojiu.webapp.admin.dto.VendorDTO#loadFeatures(FeatureContext)
	 * @see com.bojiu.webapp.game.model.GameVendor
	 * @see com.bojiu.context.model.GameType
	 * @see com.bojiu.context.model.Currency
	 * @see com.bojiu.webapp.game.entity.MerchantShareConfig
	 */
	Game("厂商游戏", Module.GAME, 1, 0, null),
	/**
	 * 创建子站点 - 创建数量
	 * <p> 存储数据结构：<code> "1" </code>
	 *
	 * @see FeatureContext#siteNum()
	 * @see com.bojiu.webapp.base.entity.Group#getMerchantNum()
	 */
	Site("创建子站点", Module.SITE_CONFIG, 0, 0, forInteger()),
	/**
	 * 前台皮肤仓库 - "亚洲经典 2/5"
	 *
	 * @see com.bojiu.context.model.TemplateType
	 * @see com.bojiu.webapp.user.model.MetaType#attach_config
	 * @see com.bojiu.webapp.user.dto.AttachConfig#getSkins()
	 */
	Skin("皮肤仓库", Module.SITE_CONFIG, 1, 0, forFront()),
	/**
	 * 注册登录
	 * 存储数据结构：<pre><code>
	 * {
	 *     "popUp": [ "0", "1", "2" ],
	 *     "registerWay": [ "0", "1", "2" ],
	 *     "loginWay": [ "0", "1", "2" ],
	 *     "thirdLogin": [ "1" ],
	 *     "captcha": [ "1" ]
	 * }
	 * </code></pre>
	 *
	 * @see com.bojiu.webapp.user.model.MetaType#register_config
	 * @see com.bojiu.webapp.user.entity.meta.RegisterCfg#getPopUp()
	 * @see FeatureItem.AuthOption
	 * @see FeatureItem.AuthOption.PopUpOption
	 * @see com.bojiu.context.model.LoginWayConfig
	 */
	Auth("注册登录", Module.SITE_CONFIG, 1, 0, forNestedMultiValues(FeatureItem.AuthOption.CACHE)),
	/**
	 * 顶部下载条
	 *
	 * @see FeatureContext#downloadBar()
	 * @see com.bojiu.webapp.user.vo.DownloadBarSegment
	 * @see com.bojiu.webapp.user.model.MetaType#download_bar_config
	 */
	DownloadBar("顶部下载条", Module.SITE_CONFIG, 0, 0, forBoolean()),
	/**
	 * 活动
	 *
	 * @see com.bojiu.context.model.PromotionType#loadFeatures(FeatureContext)
	 * @see com.bojiu.webapp.user.entity.Promotion
	 */
	Activity("活动", Module.OPERATION, 1, 0, forMultiValues(com.bojiu.context.model.PromotionType.CACHE_SHOW)),
	/**
	 * VIP等级管理
	 * <ul>
	 *
	 * <li>VIP</li>
	 *
	 * <li>VIP返利（返水）   => 配置类={@link com.bojiu.webapp.user.entity.RebateConfig }，
	 * 元数据={@link com.bojiu.webapp.user.model.MetaType#rebate_base_config }</li>
	 *
	 * <li>VIP等级管理   => {@link com.bojiu.webapp.trade.model.UserLevel } 、 {@link com.bojiu.webapp.trade.model.MajorLevel }，
	 * 元数据={@link com.bojiu.webapp.user.model.MetaType#user_upgrade_config }</li>
	 * </ul>
	 *
	 * @see FeatureItem.VipOption#loadFeatures(FeatureContext)
	 * @see FeatureContext#vip()
	 * @see FeatureContext#vipLevel()
	 * @see FeatureContext#rebate()
	 */
	Vip("VIP等级管理", Module.OPERATION, 1, 0, forMultiValues(FeatureItem.VipOption.CACHE)),
	/**
	 * 公积金
	 *
	 * @see FeatureContext#deposit()
	 * @see com.bojiu.context.model.PromotionType#DEPOSIT_POOL
	 * @see com.bojiu.webapp.user.vo.DepositPoolRuleVO
	 */
	Deposit("公积金", Module.OPERATION, 0, 0, forBoolean()),
	/**
	 * 利息宝
	 *
	 * @see FeatureContext#incomeBox()
	 * @see com.bojiu.webapp.trade.entity.UserIncomeRule
	 */
	IncomeBox("利息宝", Module.OPERATION, 0, 0, forBoolean()),
	/**
	 * 网红博主
	 *
	 * @see FeatureContext#blogger()
	 */
	Blogger("网红博主", Module.OPERATION, 0, 0, forBoolean()),
	/**
	 * 任务中心
	 *
	 * @see com.bojiu.context.model.TaskType#loadFeatures(FeatureContext)
	 */
	Task("任务中心", Module.OPERATION, 0, 0, forMultiValues(com.bojiu.context.model.TaskType.CACHE_SHOW)),
	/**
	 * 会员上传头像
	 *
	 * @see FeatureContext#userUploadAvatar()
	 */
	UserUploadAvatar("会员上传头像", Module.OPERATION, 0, 0, forBoolean()),
	/**
	 * 分享链接预览
	 *
	 * @see FeatureContext#sharePreview()
	 */
	SharePreview("分享链接预览", Module.OPERATION, 0, 0, forBoolean()),
	/**
	 * 主播试玩账号
	 *
	 * @see FeatureContext#gameTrialAccount()
	 */
	GameTrialAccount("主播试玩账号", Module.USER, 0, 0, forBoolean()),
	/**
	 * 会员提现免首充
	 *
	 * @see FeatureContext#cashWithoutRecharge()
	 */
	CashWithoutRecharge("会员提现免首充", Module.USER, 0, 0, forBoolean()),
	/**
	 * 代理模式
	 *
	 * @see com.bojiu.webapp.user.model.AgentModelType#loadFeatures(FeatureContext)
	 */
	AgentMode("代理模式", Module.AGENT, 0, 0, null),
	/**
	 * 代理前端样式
	 */
	AgentSkin("代理前端样式", Module.AGENT, 0, 0, forFront()),
	/**
	 * 更改上级代理
	 *
	 * @see FeatureContext#changeParentAgent()
	 */
	ChangeParentAgent("更改上级代理", Module.AGENT, 0, 0, forBoolean()),
	/**
	 * 支付配置
	 * 存储数据结构：<pre><code>
	 * {
	 *     "BRL": [ "GlobalPay", "PixPay" ],
	 *     "VND": [ "GlobalPay", "HopPay" ]
	 * }
	 * </code></pre>
	 *
	 * @see com.bojiu.webapp.trade.model.PayChannel#loadFeatures(FeatureContext)
	 */
	Payment("支付配置", Module.FINANCIAL, 1, 0, null),
	/**
	 * 会员稽核管理
	 *
	 * @see FeatureContext#userAudit()
	 */
	UserAudit("会员稽核管理", Module.FINANCIAL, 0, 0, forBoolean()),
	/**
	 * 风控中心
	 * <p>存储数据结构：<pre><code>
	 * {
	 *      "rtpControl":[ "PGC", "BPG", "BGT" ],
	 *      "botSpy": true,
	 *      "profitSpy": true
	 * }
	 * </code></pre>
	 *
	 * @see FeatureContext#botSpy()
	 * @see FeatureContext#profitSpy()
	 * @see com.bojiu.webapp.game.model.GameProvider#loadCtrlFeatures(FeatureContext)
	 * @see FeatureItem.RiskOption
	 * @see com.bojiu.webapp.game.model.GameProvider#CTRL_CACHE
	 */
	RiskControl("风控中心", Module.RISK_CONTROL, 0, 0, null),
	/**
	 * 抽成模式
	 *
	 * @see com.bojiu.webapp.game.model.CommissionMode
	 * <p>存储数据结构：<pre><code>
	 * {
	 * "pgc": [5,7,8,9],
	 * "hbo": [10,11,12],
	 * "mode":[1,2]
	 * }
	 * </code></pre>
	 */
	CommissionMode("抽成模式", Module.COMMISSION_MODE, 1, 0, null),
	;

	/** 功能 & 特性 名称 */
	public final String label;
	/** 所属模块 */
	public final Module module;
	/** 须要选择的最小选项数量：如果为 0 表示非必需 */
	public final int min;
	/** 允许选择的最大选项数：如果为 0 表示不限制，如果为 1 表示只能单选 */
	public final int max;
	/** 对应的配置数据处理器，有些比较复杂的可能为 null，需要在后台编辑表单时自行实现，参见 FeatureContextForm.getHandlerFor(Feature) */
	@Nullable
	public transient FeatureConfigHandler<?> handler;
	@Nullable
	public transient java.util.function.BiPredicate<FeatureConfig, String> customHasMenu;

	Feature(String label, Module module, int min, int max, @Nullable FeatureConfigHandler<?> handler) {
		this.label = label;
		this.module = module;
		this.min = min;
		this.max = max;
		this.handler = handler;
	}

	public void checkSelectedSize(int size) {
		if (size < min) {
			throw new IllegalArgumentException("[" + label + "]必须选择至少 " + min + " 个选项！");
		}
		if (max > 0 && size > max) {
			throw new IllegalArgumentException("[" + label + "]最多只能选择 " + max + " 个选项！");
		}
	}

	public boolean delegateToFront() {
		return this == Skin || this == BgSkin || this == AgentSkin;
	}

}