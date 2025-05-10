package com.candlk.webapp.action;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Ready;
import com.candlk.context.web.Jsons;
import com.candlk.context.web.ProxyRequest;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.model.ESIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestAction {

	@Value("${service.proxy.host}")
	private String host;
	@Value("${service.proxy.port}")
	private Integer port;
	@Resource
	ESEngineClient esEngineClient;

	@Ready("墙外访问测试")
	@GetMapping("/ping")
	public Messager<String> addOrEdit(ProxyRequest q) {
		final String tgMsg = "https://api.telegram.org/bot7098739919:AAG7V8jhpmhehF9Z5ZHL6YgA9qmmpkwV3Zg/sendMessage?chat_id=-1002081472730&text=这是一条测试消息&parse_mode=Markdown";
		RestTemplate restTemplate = new RestTemplate();
		SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
		reqfac.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		restTemplate.setRequestFactory(reqfac);
		JSONObject body = restTemplate.getForEntity(tgMsg, JSONObject.class).getBody();
		return Messager.OK(Jsons.encode(body));
	}

	@Ready("ES添加status字段")
	@GetMapping("/esAddStatusField")
	public Messager<Void> esAddStatusField() throws Exception {
		final ElasticsearchClient client = esEngineClient.client;
		final String indexName = ESIndex.KEYWORDS_ACCURATE_INDEX.name().toLowerCase();
		final String fieldName = "status";
		// Step 1: 获取索引映射关系
		final TypeMapping typeMapping = client.indices()
				.getMapping(GetMappingRequest.of(r -> r.index(indexName)))
				.get(indexName).mappings();
		final Map<String, Property> properties = typeMapping.properties();

		if (!properties.containsKey(fieldName)) {
			// Step 2: 添加字段
			client.indices().putMapping(PutMappingRequest.of(req -> req
					.index(indexName)
					.properties(fieldName, Property.of(p -> p.integer(i -> i)))
			));

			log.info("✅ 添加字段成功：" + fieldName);
		} else {
			log.info("ℹ️ 字段已存在：" + fieldName);
		}

		// Step 3: 更新现有文档默认值
		final UpdateByQueryResponse response = client.updateByQuery(UpdateByQueryRequest.of(req -> req
				.index(indexName)
				.script(s -> s
						// 通过脚本设置默认值
						.source(builder -> builder.scriptString("if (ctx._source.status == null) { ctx._source.status = 1; }"))
						.lang("painless")
				)
				.query(q -> q
						.bool(b -> b
								.mustNot(mn -> mn
										.exists(e -> e.field(fieldName))
								)
						)
				)
		));
		log.info("🔁 更新文档数据: " + response.updated());
		return Messager.OK();
	}

}
