package com.bojiu.webapp.user.entity;

import java.util.*;

import com.alibaba.fastjson2.JSONArray;
import com.bojiu.common.context.RequestContext;
import com.bojiu.common.model.Bean;
import com.bojiu.common.model.Status;
import com.bojiu.common.util.PropertyBean;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.model.Operation;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.bojiu.webapp.base.entity.BizEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.codeplayer.util.*;
import org.jspecify.annotations.Nullable;

/**
 * （平台 + 商户）员工操作日志
 */
@Setter
@Getter
public class AdminLog extends BizEntity {

	/** 商户ID：0 表示平台 */
	Long merchantId;
	/** （平台 + 商户）员工ID */
	Long empId;
	/** 操作类型 */
	Operation type;
	/** 操作详情（JSON数据） */
	String detail;
	/** 操作目标类型 */
	String assocType;
	/** 操作目标ID */
	Long assocId;
	/** 审核结果：1=通过；-1=不通过 */
	Integer auditResult;
	/** 备注 */
	String remark;
	/** 操作IP */
	String addIp;
	/** 模块名称 ,例如：用户管理 */
	String moduleName;
	/** 功能名,例如：会员 */
	String func;
	/** 操作行为,例如：冻结 */
	String action;

	public AdminLog init(Emp emp, Operation type, String assocType, Long assocId, String module, String func, String action) {
		this.empId = Bean.toLongId(emp);
		this.type = type;
		this.assocType = assocType;
		this.assocId = assocId;
		this.moduleName = module;
		this.func = func;
		this.action = action;
		return this;
	}

	public static AdminLog of(Emp user, Operation type, BaseEntity assocObj, String module, String func, String action) {
		return of(user, type, assocObj.entityName(), assocObj.getId(), module, func, action);
	}

	public static AdminLog of(Emp user, Operation type, String assocType, Long assocId, String module, String func, String action) {
		AdminLog log = new AdminLog().init(user, type, assocType, assocId, module, func, action);
		log.setMerchantId(ContextImpl.currentMerchantId());
		return log;
	}

	public static AdminLog of(Emp user, boolean addOrEdit, String assocType, Long assocId, String module, String func, String action) {
		return of(user, addOrEdit ? Operation.ADD : Operation.EDIT, assocType, assocId, module, func, action);
	}

	public static AdminLog of(Emp user, boolean addOrEdit, BaseEntity assocObj, String module, String func, String action) {
		return of(user, addOrEdit, assocObj.entityName(), assocObj.getId(), module, func, action);
	}

	public static AdminLog of(Emp user, boolean addOrEdit, BaseEntity assocObj, ModuleInfo module) {
		return of(user, addOrEdit, assocObj.entityName(), assocObj.getId(), module.module, module.func, module.action);
	}

	public static AdminLog ofToggle(Emp user, boolean enabled, BaseEntity assocObj, String module, String func) {
		return of(user, Operation.TOGGLE, assocObj, module, func, enabled ? "启用" : "停用");
	}

	public AdminLog init(Date addTime, String addIp) {
		super.initTime(addTime);
		this.addIp = addIp;
		return this;
	}

	public AdminLog init(RequestContext req) {
		if (empId == null) {
			empId = Bean.toLongId(req.sessionUser());
		}
		return init(req.now(), req.clientIP());
	}

	public void initDefault() {
		if (addIp == null) {
			addIp = RequestContext.get().clientIP();
		}
		detail = StringUtil.toString(detail);
		remark = StringUtil.toString(remark);
		super.initTime(addTime);
		super.initActiveStatus();
	}

	public AdminLog appendRemark(String remark) {
		if (StringUtil.notEmpty(remark)) {
			this.remark = StringUtil.isEmpty(this.remark) ? remark : StringUtil.concat(this.remark, " | ", remark);
		}
		return this;
	}

	public void init(Long userId, Operation type, String detail, String remark, Date now) {
		this.empId = userId;
		this.type = type;
		this.detail = StringUtil.toString(detail);
		this.remark = StringUtil.toString(remark);
		this.initTime(now);
		super.initActiveStatus();
	}

	public AdminLog setRemark(String remark) {
		this.remark = StringUtil.toString(remark);
		return this;
	}

	public AdminLog setDiffs(String field, String label, Object old, Object now) {
		setDiffs(FieldDiff.of(field, label, old, now));
		return X.castType(this);
	}

	public AdminLog setDiffs(List<FieldDiff> diffs) {
		this.detail = X.isValid(diffs) ? Jsons.encode(diffs) : "";
		return X.castType(this);
	}

	public AdminLog setDiffs(FieldDiff... diffs) {
		final List<FieldDiff> list = new ArrayList<>(diffs.length);
		for (FieldDiff diff : diffs) {
			if (diff != null) {
				list.add(diff);
			}
		}
		return setDiffs(list);
	}

	public boolean hasDetails() {
		return StringUtil.notEmpty(detail);
	}

	public static String[] getDetail_(String detail) {
		if (StringUtil.isEmpty(detail)) {
			return null;
		}
		StringBuilder old = new StringBuilder();
		StringBuilder current = new StringBuilder();
		try {
			List<FieldDiff> logInfos = JSONArray.parseArray(detail, FieldDiff.class);
			final String sep = "\\r\\n";
			for (FieldDiff info : logInfos) {
				old.append(info.getLabel()).append(':').append(info.getOld()).append(sep);
				current.append(info.getLabel()).append(':').append(info.getNow()).append(sep);
			}
			return new String[] { old.toString(), current.toString() };
		} catch (Exception ignored) {
			return null;
		}
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	public static class FieldDiff {

		public static final int OLD_INDEX = 0;
		public static final int CURRENT_INDEX = 1;

		// 修改字段 字段描述 旧值 新值
		protected String field, label;
		protected Object old, now;

		private FieldDiff(String field, String label, Object old, Object now) {
			this.field = field;
			this.label = label;
			this.old = old;
			this.now = now;
		}

		public void setHistory(Object old) {
			this.old = old;
		}

		public void setCurrent(Object now) {
			this.now = now;
		}

		@Nullable
		public static FieldDiff of(String field, String label, Object old, Object now) {
			if (Objects.equals(old, now)) {
				return null;
			}
			return new FieldDiff(field, label, old, now);
		}

		public static FieldDiff ofStatus(String del, String label, Boolean old, Boolean now) {
			if (Objects.equals(old, now)) {
				return null;
			}
			return new FieldDiff(del, label, X.map(old, o -> Status.of(o).label), X.map(now, o -> Status.of(o).label));
		}

		@Nullable
		public static FieldDiff of(String field, Integer id, Object old, Object now) {
			return of(field, id.toString(), ofValue(old), ofValue(now));
		}

		/**
		 * 比对两个对象的指定字段，记录差异
		 *
		 * @param old 旧对象
		 * @param updated 新对象
		 * @param fields 比对的字段，格式为：字段|方法,描述。如:name,名称，或:getName(),名称
		 */
		public static List<FieldDiff> diff(Object old, Object updated, String... fields) {
			return diff(old, updated, true, fields);
		}

		/**
		 * 比对两个对象的指定字段，记录差异
		 *
		 * @param old 旧对象
		 * @param updated 新对象
		 * @param hasLabel 如果为<code>true</code>，则表示 fields 传入的是依次 <code> field1, label1, field2, label2 </code> 形式的键值对，
		 * 否则只需传入 <code> field1、field2 </code>
		 * @param fields 属性名 或 属性名 + 中文名称 的多个组合对，属性名 可以是 field name
		 */
		public static List<FieldDiff> diff(Object old, Object updated, boolean hasLabel, String... fields) {
			Assert.isTrue(!hasLabel || (fields.length & 1) == 0, "指定键值的参数个数必须为偶数!");
			List<FieldDiff> list = new ArrayList<>();
			Class<?> oldClass = null;
			PropertyBean oldAsm = null, newAsm = null;
			if (old != null) {
				oldAsm = PropertyBean.getInstance(oldClass = old.getClass());
			}
			if (updated != null) {
				Class<?> newClass = updated.getClass();
				newAsm = oldClass == newClass ? oldAsm : PropertyBean.getInstance(newClass);
			}
			for (int i = 0; i < fields.length; ) {
				String property = fields[i++], label = hasLabel ? fields[i++] : null;
				Object oldVal = old == null ? null : oldAsm.getProperty(old, property),
						updatedVal = updated == null ? null : newAsm.getProperty(updated, property);
				if (!Objects.equals(oldVal, updatedVal)) {
					list.add(new FieldDiff(property, label, oldVal, updatedVal));
				}
			}
			return list;
		}

		private static Object ofValue(Object val) {
			return val instanceof Bean ? ((Bean<?>) val).getId() : val;
		}

	}

	public record ModuleInfo(String module, String func, String action) {

		public static ModuleInfo of(String module, String func, String action) {
			return new ModuleInfo(module, func, action);
		}

	}

}