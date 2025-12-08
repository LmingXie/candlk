package com.bojiu.webapp.user.action;

import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
@RestController
@RequestMapping("/app")
public class AppAction {

	@GetMapping("/layout")
	@Permission(Permission.NONE)
	public Messager<JSONObject> layout(final ProxyRequest q) {
		return Messager.exposeData(JSONObject.of("layout", "layout"));
	}

	/**
	 * 递归方法将 XML 元素转换为 JSONObject 或 JSONArray
	 *
	 * @return {@link JSONObject} 或 null 或 字符串
	 */
	static Object xmlNodeToJson(Element element, final boolean playLogs) {
		final NodeList children = element.getChildNodes();
		final int length = children.getLength();

		if (length == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
			// 如果元素只有一个文本节点，直接返回文本内容
			return element.getTextContent();
		}

		final JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < length; i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) node;
				final String tagName = child.getTagName();

				if (playLogs && "GameResult".equals(tagName)) {
					// 跳过 GameResult 节点解析
					continue;
				}

				final Object childNode = xmlNodeToJson(child, playLogs);
				final Object old = jsonObject.put(tagName, childNode);
				if (old != null) { // 之前已存在该 tagName，将已有的元素添加到 JSONArray 中
					final JSONArray list;
					if (old instanceof JSONArray array) {
						list = array;
					} else {
						list = new JSONArray(length);
						list.add(old);
					}
					list.add(childNode);
					jsonObject.put(tagName, list);
				}
			}
		}

		return jsonObject.isEmpty() ? null : jsonObject;
	}

	static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	static protected JSONObject responseBodyToJSON(String responseBody) {
		// 针对没有游戏记录的响应，进行特殊处理，节省XML解析开销
		final boolean playLogs = StringUtils.endsWith(responseBody, "GetAllBetDetailsForTimeIntervalResponse>");
		if (playLogs && responseBody.contains("<BetDetailList />") && responseBody.contains("<ErrorMsgId>0</ErrorMsgId>")) {
			return JSONObject.of("ErrorMsgId", "0");
		}
		final Document doc;
		try {
			doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseBody)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
		final Element root = doc.getDocumentElement();
		return (JSONObject) xmlNodeToJson(root, playLogs);
	}

	public static void main(String[] args) throws IOException {
		final String xmlPath = "C:\\Users\\Administrator\\Documents\\Fiddler2\\Captures\\rate_list.xml";
		final String fileContent = FileUtil.readContent(new File(xmlPath));
		final String beginStr = "<original>";
		int begin = fileContent.lastIndexOf(beginStr);
		int end = fileContent.lastIndexOf("</original>");
		final String gameList = fileContent.substring(begin + beginStr.length(), end);
		// System.out.println(gameList);
		JSONObject root = Jsons.parseObject(gameList);
		int size = root.size();
		System.out.println("总比赛场数：" + size);
		for (int i = 0; i < size; i++) {
			System.out.println(root.get("GAME_" + i));
		}
		/*
		让球盘：
			RATIO_R = 0.5
			1/1.5：
				主队 = IOR_RH = -1/1.5
				客队 = IOR_RC = +1/1.5

			RATIO_R = 1：
				主队 = IOR_RH = -1（此时应该+1）
				客队 = IOR_RC = +1（取原始IOR_RC）
		 */
	}

}
