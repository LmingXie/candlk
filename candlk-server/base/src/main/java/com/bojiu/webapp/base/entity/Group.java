package com.bojiu.webapp.base.entity;

import java.util.Date;
import java.util.Map;

import com.bojiu.context.model.Currency;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.CollectionUtil;

/**
 * 集团表
 *
 * @author wsl
 * @since 2024-06-06
 */
@Setter
@Getter
public class Group extends BizEntity {

	/** 集团名称 */
	String name;
	/** 商户数量 */
	Integer merchantNum;
	/** 货币 */
	Currency currency;
	/** 老状态，维护与恢复功能使用 */
	Integer oldStatus;
	/** 商户关闭时间 */
	Date closeTime;
	/** 持有人 */
	String holder;
	/** 推荐方式 1 商务推荐 2 代理推荐 3商户推荐 4广告 */
	Integer recommenderType;
	/** 备注 */
	String remark;
	/** 品牌 */
	Long brandId;

	public static final String NAME = "name";
	public static final String MERCHANT_NUM = "merchant_num";
	public static final String CURRENCY = "currency";
	public static final String OLD_STATUS = "old_status";
	public static final String CLOSE_TIME = "close_time";
	public static final String HOLDER = "holder";
	public static final String RECOMMENDER_TYPE = "recommender_type";
	public static final String BRAND_ID = "brand_id";

	public static Map<String, Object> asEmbed(Group g) {
		return g == null ? null : CollectionUtil.asHashMap(
				"id", g.getId(),
				"name", g.getName()
		);
	}

}