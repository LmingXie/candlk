package com.candlk.webapp;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.rules.*;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.candlk.common.gencode.tool.MvcTemplateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;

@Slf4j
public class CodeGeneratorTest {

	@Test
	public void winCreate() {
		MvcTemplateGenerator.GeneratorConfig config = initDbConfig();
		generate("", config, "x_tweet_word");
	}

	private MvcTemplateGenerator.GeneratorConfig initDbConfig() {
		final Properties props = MvcTemplateGenerator.getLocalProperties();
		props.putAll(Map.of(
				"jdbc-url", "jdbc:mysql://127.0.0.1:3306/x_cockpit_local"
		));
		return MvcTemplateGenerator.GeneratorConfig.get(props);
	}

	public static void generate(String author, @Nullable MvcTemplateGenerator.GeneratorConfig generatorConfig, String... tableNames) {
		MvcTemplateGenerator.GeneratorConfig config = generatorConfig == null ? MvcTemplateGenerator.GeneratorConfig.get() : generatorConfig;
		DataSourceConfig.Builder dsBuilder = config.getDataSource() == null
				? new DataSourceConfig.Builder(config.getDsProps().getUrl(), config.getDsProps().getUsername(), config.getDsProps().getPassword())
				: new DataSourceConfig.Builder(config.getDataSource());
		FastAutoGenerator autoGenerator = FastAutoGenerator.create(dsBuilder
						.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
							int typeCode = metaInfo.getJdbcType().TYPE_CODE;
							if (typeCode == Types.SMALLINT || typeCode == Types.TINYINT) {
								// 自定义类型转换
								return DbColumnType.INTEGER;
							}
							return typeRegistry.getColumnType(metaInfo);
						})
				).globalConfig(builder -> builder.author(author)
						.commentDate("yyyy-MM-dd")
						.dateType(DateType.ONLY_DATE)
						.outputDir(config.getOutputDir())
						.disableOpenDir())
				.packageConfig((builder) -> builder.parent(config.getParentPackage())
						.controller("action")
						.service("service")
						.serviceImpl("service.impl")
						.entity("entity")
						.mapper("dao"))
				.strategyConfig(builder -> builder.addTablePrefix("x_")
						.addInclude(tableNames).enableCapitalMode()

						.entityBuilder()
						.disableSerialVersionUID()
						.enableFileOverride()
						.superClass("com.candlk.webapp.base.entity.BaseEntity")
						.enableLombok()
						.naming(NamingStrategy.underline_to_camel)
						.columnNaming(NamingStrategy.underline_to_camel)
						.enableColumnConstant()

						.mapperBuilder()
						.enableFileOverride()
						.superClass("com.candlk.webapp.base.dao.BaseDao")
						.mapperAnnotation(Mapper.class)
						.enableBaseColumnList()
						.enableBaseResultMap().formatMapperFileName("%sDao")
						.formatXmlFileName("%sMapper")

						.serviceBuilder()
						.enableFileOverride()
						.superServiceClass("com.candlk.webapp.base.service.BaseService")
						.formatServiceFileName("%sService")
						.superServiceImplClass("com.candlk.webapp.base.service.BaseServiceImpl")
						.formatServiceImplFileName("%sServiceImpl")

						.controllerBuilder()
						.enableFileOverride()
						.superClass("com.candlk.webapp.base.action.BaseAction")
						.enableRestStyle()
						.formatFileName("%sAction")).templateConfig(builder -> builder.entity("templates/entity")
						.mapper("templates/dao")
						.service("templates/service")
						.serviceImpl("templates/service.impl")
						.controller("templates/action")
				).templateEngine(new FreemarkerTemplateEngine());
		autoGenerator.execute();

		try {
			Desktop.getDesktop().open(new File(config.getOutputDir()));
		} catch (IOException var6) {
			log.error("打开文件夹[" + config.getOutputDir() + "]失败", var6);
		}
	}

}
