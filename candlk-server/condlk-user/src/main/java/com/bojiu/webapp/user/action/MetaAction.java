package com.bojiu.webapp.user.action;

import java.util.List;
import javax.annotation.Resource;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.model.Option;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.form.MetaForm;
import com.bojiu.webapp.user.form.query.MetaQuery;
import com.bojiu.webapp.user.model.MetaType;
import com.bojiu.webapp.user.service.*;
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
	@Resource
	MetaService metaService;

	@Ready("元数据管理")
	@GetMapping("/typeList")
	@Permission(Permission.USER)
	public Messager<List<Option<String>>> typeList() {
		return Messager.exposeData(Option.toMetas(MetaType.CACHE, null));
	}

	@Ready("元数据编辑")
	@PostMapping("/edit")
	@Permission(Permission.USER)
	public Messager<Meta> edit(ProxyRequest q, @Validated MetaForm form) {
		form.merchantId = form.type.isolationUser ? q.getSessionUser().getId() : PLATFORM_ID;
		final Messager<Meta> msger = metaAdminService.addOrEdit(null, form.copyTo(Meta::new), true);
		if (msger.isOK()) {
			GlobalCacheSyncService.refreshCache(msger.data());
			msger.setMsg(I18N.msg(AdminI18nKey.OPERATE_SUCCESS)).setURLBack();
		}
		return msger;
	}

	@Ready("单个允许访问的配置")
	@GetMapping("/types")
	@Permission(Permission.USER)
	public Messager<MetaVO> types(ProxyRequest q, MetaForm form) {
		final Long merchantId = form.type.isolationUser ? q.getSessionUser().getId() : PLATFORM_ID;
		return Messager.exposeData(MetaVO.fromItem(metaAdminService.get(merchantId, form.getType().value, form.getName())));
	}

	@Ready("元数据列表")
	@GetMapping("/typeItemList")
	@Permission(Permission.USER)
	public Messager<List<MetaVO>> typeItemList(ProxyRequest q, MetaQuery query) {
		final Long merchantId = query.type.isolationUser ? q.getSessionUser().getId() : PLATFORM_ID;
		final List<Meta> list = metaService.find(merchantId, query.type, query.name);
		return Messager.exposeData(CollectionUtil.toList(list, MetaVO::from));
	}

}