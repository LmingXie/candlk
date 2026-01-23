package com.bojiu.webapp.user.service;

import java.util.Date;
import javax.annotation.Resource;

import com.bojiu.common.model.Bean;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.model.RedisKey;
import com.bojiu.webapp.user.entity.*;
import com.bojiu.webapp.user.model.MetaType;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * 元数据表 服务实现类
 *
 * @since 2023-04-17
 */
@Slf4j
@Service
public class MetaAdminService /*extends BaseServiceImpl<Meta, MetaDao, Long>*/ {

	// @Resource
	// AdminLogService adminLogService;
	@Resource
	MetaService metaService;

	public Messager<Meta> addOrEdit(Emp emp, Meta input, boolean isPermission) {
		return addOrEdit(emp, input, false, null, isPermission);
	}

	/**
	 * 增加、编辑元数据（自动装配时间与status字段）
	 */
	public Messager<Meta> addOrEdit(Emp emp, Meta input, boolean write, AdminLog.ModuleInfo logModel, boolean isPermission) {
		final boolean addOrEdit = !Bean.hasId(input);
		Long merchantId = input.getMerchantId();
		MetaType type = input.getType();
		if (addOrEdit) {
			if (!Bean.isValidId(merchantId)) {
				input.setMerchantId(merchantId);
			}
			if (StringUtil.isEmpty(input.getName())) {
				input.setName(type.name());
			}
			if (StringUtil.isEmpty(input.getLabel())) {
				input.setLabel(type.getLabel());
			}
		}
		final Meta target = addOrEdit ? input : Assert.notNull(metaService.getCached(merchantId, type));
		// emp.assertCanAccess(target.getMerchantId()); TODO 登录，避免暴露敏感数据

		// if (addOrEdit && Common.isFieldChanged(target, Meta::getName, input.getName()) && exist(input.getId(), null, input.getName(), type, null, merchantId)) {
		// 	return Messager.error(I18N.msg(AdminI18nKey.NAME_EXIST));
		// }

		target.init(input, new Date());
		RedisUtil.opsForHash().put(RedisKey.META_PREFIX + merchantId + ":" + type.value, input.getName(), input.getValue());
		// super.saveOrUpdate(target, !addOrEdit);
		// if (write && logModel != null) {
		// 	AdminLog l = AdminLog.of(emp, addOrEdit, target, logModel);
		// 	adminLogService.log(l);
		// }
		return Messager.hideData(target);
	}

	public Meta get(Long merchantId, Integer type, String name) {
		// var wrapper = smartQueryWrapper().eq(TYPE, type).eq(MERCHANT_ID, merchantId).eq(NAME, name);
		// return baseDao.selectOne(wrapper);
		final MetaType metaType = MetaType.of(type);
		Meta meta = metaService.getCached(merchantId, metaType, name == null ? metaType.name() : name);
		if (meta == null) {
			meta = MetaService.getDefaultMeta(merchantId, metaType, name);
		}
		return meta;
	}

}