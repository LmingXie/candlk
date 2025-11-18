package com.bojiu;

import java.util.Properties;

import com.bojiu.common.gencode.tool.MvcTemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class CodeGeneratorTest {

	@Test
	public void winCreate() {
		MvcTemplateGenerator.GeneratorConfig config = initDbConfig();
		MvcTemplateGenerator.generate("", config, "tg_user");
	}

	private MvcTemplateGenerator.GeneratorConfig initDbConfig() {
		final Properties props = MvcTemplateGenerator.getLocalProperties();
		props.put("jdbc-url", "jdbc:mysql://127.0.0.1:3607/tg_local");
		props.put("password", "PzxELStGI2MzEhoFBQk4Bw");
		return MvcTemplateGenerator.GeneratorConfig.get(props).setTablePrefix("tg_");
	}

}