package com.bojiu.webapp.user.action;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.Option;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.user.entity.Emp;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.form.MetaForm;
import com.bojiu.webapp.user.form.query.MetaQuery;
import com.bojiu.webapp.user.model.MetaType;
import com.bojiu.webapp.user.service.GlobalCacheSyncService;
import com.bojiu.webapp.user.service.MetaAdminService;
import com.bojiu.webapp.user.vo.MetaVO;
import me.codeplayer.util.CollectionUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;

/**
 * 元数据表 控制器
 *
 * @since 2023-04-17
 */
@RestController
@RequestMapping("/meta")
public class MetaAction extends BaseAction {

	@Resource
	MetaAdminService metaAdminService;

	@Ready("元数据列表")
	@GetMapping("/list")
	@Permission(Permission.SYSTEM)
	public Messager<Page<MetaVO>> list(ProxyRequest q, MetaQuery query) {
		q.applyMerchantId(query::setMerchantId);
		final Page<Meta> page = metaAdminService.findPage(q.getPage(), query);
		return Messager.exposeData(page.transformAndCopy(MetaVO::new));
	}

	@Ready("元数据管理")
	@GetMapping("/typeList")
	@Permission(Permission.NONE)
	public Messager<List<Option<String>>> typeList() {
		return Messager.exposeData(Option.toMetas(MetaType.CACHE, null));
	}

	@Ready("元数据键值列表")
	@GetMapping("/itemList")
	@Permission(Permission.NONE)
	public Messager<Map<String, MetaVO>> itemList(ProxyRequest q, MetaQuery query) {
		final List<Meta> list = metaAdminService.find(query.value, query.name, query.type, query.status, query.merchantId);
		return Messager.exposeData(CollectionUtil.toHashMap(list, Meta::getLabel, MetaVO::from));
	}

	@Ready(value = "元数据键值列表（查询所有站点）", merchantIdRequired = false)
	@GetMapping("/allItemList")
	@Permission(Permission.NONE)
	public Messager<Map<String, List<MetaVO>>> allItemList(ProxyRequest q, MetaQuery query) {
		final List<Meta> list = metaAdminService.find(query.value, query.name, query.type, query.status, query.toMerchantIds());
		Map<String, List<MetaVO>> map = list.stream().map(MetaVO::from).collect(Collectors.groupingBy(MetaVO::getLabel));
		return Messager.exposeData(map);
	}

	@Ready("元数据编辑")
	@PostMapping("/edit")
	@Permission(Permission.NONE)
	public Messager<Meta> edit(ProxyRequest q, @Validated MetaForm form) {
		final Emp emp = q.getSessionUser();
		form.merchantId = PLATFORM_ID;
		final Messager<Meta> msger = metaAdminService.addOrEdit(emp, form.copyTo(Meta::new), true);
		if (msger.isOK()) {
			GlobalCacheSyncService.refreshCache(msger.data());
			msger.setMsg(I18N.msg(AdminI18nKey.OPERATE_SUCCESS)).setURLBack();
		}
		return msger;
	}

	@Ready("元数据删除")
	@PostMapping("/del")
	@Permission(Permission.NONE)
	public Messager<String> del(ProxyRequest q, Long id) {
		final Emp emp = q.getSessionUser();
		Meta meta = metaAdminService.get(id);
		emp.assertCanAccess(meta.getMerchantId());
		boolean result = metaAdminService.deleteById(id);
		if (result) {
			GlobalCacheSyncService.refreshCache(meta);
		}
		return Messager.<String>OK().setMsg(I18N.msg(result ? AdminI18nKey.OPERATE_SUCCESS : AdminI18nKey.OPERATE_FAIL));
	}

	@Ready("单个允许访问的配置")
	@GetMapping("/types")
	@Permission(Permission.NONE)
	public Messager<MetaVO> types(ProxyRequest q, MetaForm form) {
		return Messager.exposeData(MetaVO.fromItem(metaAdminService.get(q.getMerchantId(), form.getType().value, null)));
	}

	@Ready("单个允许访问编辑")
	@PostMapping("/hallEdit")
	@Permission(Permission.NONE)
	public Messager<Meta> hallEdit(ProxyRequest q, @Validated MetaForm form) {
		form.merchantId = q.getMerchantId();
		final Messager<Meta> msger = metaAdminService.addOrEdit(q.getSessionUser(), form.copyTo(Meta::new));
		if (msger.isOK()) {
			GlobalCacheSyncService.refreshCache(msger.data());
		}
		return msger;
	}

	@Ready("元数据详情")
	@GetMapping("/data")
	@Permission(Permission.NONE)
	public Messager<MetaVO> data(final ProxyRequest q, Long id) {
		Meta meta = metaAdminService.get(id);
		q.getSessionUser().assertCanAccess(meta.getMerchantId());
		if (!meta.canSeeFor(meta.getMerchantId())) {
			meta = null;
		}
		return Messager.OKOrNull(meta).transform(MetaVO::from);
	}

}