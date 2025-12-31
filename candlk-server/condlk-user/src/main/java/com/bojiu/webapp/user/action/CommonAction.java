package com.bojiu.webapp.user.action;

import java.util.*;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import com.bojiu.common.model.Messager;
import com.bojiu.common.web.Ready;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.auth.CloseAccounts;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.model.*;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.job.BetMatchJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonAction extends BaseAction {

	@PostConstruct
	public void init() {
		Language[] languages = { Language.zh, Language.en, Language.pt, Language.vi };
		for (Language language : languages) {
			allowEnumMetaTypes.computeIfAbsent(language, k -> new HashMap<>());
			final Supplier<List<Option<String>>> langOptions = () -> {
				MerchantContext context = MerchantContext.get(ContextImpl.currentMerchantId());
				Language[] merLan = context.getLanguages();
				return toOptions(s -> Option.of(String.valueOf(s.getValue()), s.getLabel()), merLan);
			};
			putMetaRaw(language, "merLanguage", langOptions);
			putMeta(language, "language", null, Language.CACHE);
			putMeta(language, "allPair", BetMatchJob.ALL_PAIR);
			putMeta(language, "allResult", HedgingDTO.Odds.ALL_RESULT);

		}
	}

	/**
	 * @param types 类型名称，如果多个以逗号分隔
	 */
	@Ready("获取元数据")
	@GetMapping("/metas")
	@CloseAccounts
	@Permission(Permission.NONE)
	public Messager<Map<String, Collection<Option<String>>>> metas(ProxyRequest q, String types) {
		return exposeMetas(q.getLanguage(), types);
	}

}