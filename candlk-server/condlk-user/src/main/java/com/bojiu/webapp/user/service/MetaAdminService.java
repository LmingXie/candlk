package com.bojiu.webapp.user.service;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bojiu.common.context.I18N;
import com.bojiu.common.dao.MybatisUtil;
import com.bojiu.common.dao.SmartQueryWrapper;
import com.bojiu.common.model.*;
import com.bojiu.common.util.*;
import com.bojiu.common.web.Page;
import com.bojiu.context.AppRegion;
import com.bojiu.context.brand.FeatureContext;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.entity.MetaValue;
import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.MetaDao;
import com.bojiu.webapp.user.entity.*;
import com.bojiu.webapp.user.form.query.MetaQuery;
import com.bojiu.webapp.user.model.MetaType;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bojiu.webapp.user.entity.Meta.*;

/**
 * 元数据表 服务实现类
 *
 * @since 2023-04-17
 */
@Slf4j
@Service
public class MetaAdminService extends BaseServiceImpl<Meta, MetaDao, Long> {

	@Resource
	AdminLogService adminLogService;
	@Resource
	MetaService metaService;

	@Transactional
	public Messager<Meta> addOrEdit(Emp emp, Meta input) {
		return addOrEdit(emp, input, false, null, false);
	}

	@Transactional
	public Messager<Meta> addOrEdit(Emp emp, Meta input, AdminLog.ModuleInfo logModel) {
		return addOrEdit(emp, input, true, logModel, false);
	}

	@Transactional
	public Messager<Meta> addOrEdit(Emp emp, Meta input, boolean isPermission) {
		return addOrEdit(emp, input, false, null, isPermission);
	}

	/**
	 * 增加、编辑元数据（自动装配时间与status字段）
	 */
	@Transactional
	public Messager<Meta> addOrEdit(Emp emp, Meta input, boolean write, AdminLog.ModuleInfo logModel, boolean isPermission) {
		final boolean addOrEdit = !Bean.hasId(input);
		Long merchantId = input.getMerchantId();
		if (addOrEdit) {
			if (!Bean.isValidId(merchantId)) {
				input.setMerchantId(merchantId);
			}
			if (StringUtil.isEmpty(input.getName())) {
				input.setName(input.getType().name());
			}
			if (StringUtil.isEmpty(input.getLabel())) {
				input.setLabel(input.getType().getLabel());
			}
		}
		final Meta target = addOrEdit ? input : Assert.notNull(getByIdAndMid(input.getId(), merchantId));
		// emp.assertCanAccess(target.getMerchantId()); TODO 登录，避免暴露敏感数据

		if (addOrEdit && Common.isFieldChanged(target, Meta::getName, input.getName()) && exist(input.getId(), null, input.getName(), input.getType(), null, merchantId)) {
			return Messager.error(I18N.msg(AdminI18nKey.NAME_EXIST));
		}

		target.init(input, new Date());
		super.saveOrUpdate(target, !addOrEdit);
		if (write && logModel != null) {
			AdminLog l = AdminLog.of(emp, addOrEdit, target, logModel);
			adminLogService.log(l);

		}
		return Messager.hideData(target);
	}

	/** 配置编辑并同步修改 */
	private <T extends MetaValue<T>> Messager<Meta> toggleEdit(Meta old, Meta input, boolean togglePlatform, Class<T> clazz) {
		boolean hasId = Bean.hasId(old);
		Date now = RequestContextImpl.get().now();
		if (!hasId && togglePlatform) {
			Meta meta = get(Merchant.PLATFORM_ID, input.getType().value, null);
			meta.setMerchantId(input.getMerchantId());
			meta.setId(null);
			old = meta;
			old.setAddTime(now);
		}
		T oldCfg = old.getParsedValue(clazz);
		T newCfg = input.getParsedValue(clazz);
		oldCfg.init(newCfg);
		old.setValue(Jsons.encodeRaw(oldCfg));
		old.initTime(now);
		super.saveOrUpdate(old, hasId);
		return Messager.hideData(old);
	}

	/**
	 * 添加或编辑元数据
	 */
	@Transactional
	public void addOrEdit(Emp emp, Meta input, AdminLog.ModuleInfo moduleInfo, List<AdminLog.FieldDiff> diffs) {
		final boolean hasId = input.hasValidId();
		this.saveOrUpdate(input, hasId);
		AdminLog al = AdminLog.of(emp, !hasId, input, moduleInfo).setDiffs(diffs);
		adminLogService.log(al);
	}

	/**
	 * 添加或编辑元数据
	 */
	@Transactional
	public void addOrEdit(Emp emp, Meta meta, AdminLog.ModuleInfo moduleInfo, AdminLog.FieldDiff... diffs) {
		addOrEdit(emp, meta, moduleInfo, X.isValid(diffs) ? Arrays.asList(diffs) : null);
	}

	/**
	 * 根据查询条件返回任务规则列表
	 */
	public Page<Meta> findPage(Page<Meta> page, MetaQuery query) {
		SmartQueryWrapper<Meta> wrapper = wrapperFor(query.value, query.label, query.name, query.type, query.status, query.merchantId).apply(query.applyParams != null, query.applySql, query.applyParams)
				.orderByAsc(TYPE, ID);
		return this.selectPage(page, wrapper);
	}

	public List<Meta> find(String value, String name, MetaType type, Integer status, Long... merchantIds) {
		SmartQueryWrapper<Meta> wrapper = wrapperFor(value, null, name, type, status, merchantIds).orderByAsc(TYPE, ID);
		return this.selectList(wrapper);
	}

	public List<Meta> find(MetaType type, Long... merchantIds) {
		var wrapper = wrapperFor(null, null, null, type, null, merchantIds).orderByDesc(ID).orderByAsc(TYPE);
		return this.selectList(wrapper);
	}

	public SmartQueryWrapper<Meta> wrapperFor(String value, String label, String name, MetaType type, Integer status, Long... merchantIds) {
		return smartQueryWrapper().eq(TYPE, type).ins(MERCHANT_ID, merchantIds).eq(VALUE, value).eq(LABEL, label).eq(NAME, name).eq(STATUS, status);
	}

	public boolean exist(Long neId, String value, String name, MetaType type, Integer status, Long merchantId) {
		SmartQueryWrapper<Meta> wrapper = wrapperFor(value, null, name, type, status, null, null, merchantId).ne(ID, neId);
		return this.exists(wrapper);
	}

	public List<Meta> findByName(String name, MetaType type, Long merchantId, String ext) {
		return selectList(smartQueryWrapper().eq(TYPE, type).eq(MERCHANT_ID, merchantId).eq(EXT, ext).likeRight(NAME, name).orderByAsc(ID));
	}

	public Meta get(Long merchantId, Integer type, String name) {
		var wrapper = smartQueryWrapper().eq(TYPE, type).eq(MERCHANT_ID, merchantId).eq(NAME, name);
		return baseDao.selectOne(wrapper);
	}

	public List<Meta> get(Collection<Long> merchantId, Integer type, String name) {
		var wrapper = smartQueryWrapper().eq(TYPE, type).in(MERCHANT_ID, merchantId).eq(NAME, name);
		return baseDao.selectList(wrapper);
	}

	/**
	 * 初始化商户元数据
	 */
	@Transactional
	public void initMetaTypes(Long merchantId, String types) {
		baseDao.initCopyFromGlobal(merchantId, types);
	}

	/**
	 * 初始化商户元数据从指定商户配置中获取
	 */
	@Transactional
	public void initCopyFromMerchant(Long merchantId, Long orgMerchantId, String types) {
		baseDao.initCopyFromMerchant(merchantId, orgMerchantId, types);
	}

}