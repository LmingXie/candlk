package com.candlk.common.gencode.tool;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 代码生成器，修改mybatis中的配置生成对应代码
 */
@Slf4j
public class MvcTemplateGenerator {

	/**
	 * 需要修改 mybatis-plus-generator.properties 内部配置文件
	 */
	public static void main(String[] args) {
		generate("lgy", null, "dt_verify_reason");
	}

	public static void generate(String author, @Nullable GeneratorConfig generatorConfig, String... tableNames) {
		// 用来获取Mybatis-Plus.properties文件的配置信息
		final GeneratorConfig config = generatorConfig == null ? GeneratorConfig.get() : generatorConfig;

		FastAutoGenerator autoGenerator = config.getDataSource() == null
				? FastAutoGenerator.create(config.getDsProps().getUrl(), config.getDsProps().getUsername(), config.getDsProps().getPassword())
				: FastAutoGenerator.create(new DataSourceConfig.Builder(config.getDataSource()));

		autoGenerator
				.globalConfig(builder -> builder
						.author(author)
						.commentDate("yyyy-MM-dd")
						.dateType(DateType.ONLY_DATE)
						.fileOverride()
						.outputDir(config.getOutputDir())
						.disableOpenDir()
						.enableSwagger())
				.packageConfig(builder -> builder
						.parent(config.getParentPackage())
						.controller("action")
						.service("service")
						.serviceImpl("service")
						.entity("entity")
						.mapper("dao")
				).strategyConfig(builder -> builder
						.addTablePrefix("dt_", "tr_", "im_")
						.addInclude(tableNames) // 需要生成的表
						.enableCapitalMode()
						//
						.entityBuilder().superClass("com.candlk.webapp.base.entity.BaseEntity")
						.enableLombok()
						.naming(NamingStrategy.underline_to_camel)
						.columnNaming(NamingStrategy.underline_to_camel)
						.enableColumnConstant()
						//
						.mapperBuilder().superClass("com.candlk.webapp.base.dao.BaseDao")
						.enableMapperAnnotation()
						.enableBaseColumnList()
						.enableBaseResultMap()
						.formatMapperFileName("%sDao")
						.formatXmlFileName("%sMapper")
						//
						.serviceBuilder().superServiceClass("com.candlk.webapp.base.service.BaseServiceImpl")
						.formatServiceFileName("%sService")
						.superServiceImplClass("com.candlk.webapp.base.service.BaseServiceImpl")
						.formatServiceImplFileName("%sService")
						//
						.controllerBuilder().superClass("com.candlk.webapp.base.action.BaseAction")
						.enableRestStyle()
						.formatFileName("%sAction")
				)
				.templateConfig(builder -> builder
						.entity("templates/entity")
						.mapper("templates/dao")
						.service("templates/service")
						.disable(TemplateType.SERVICE)
						.serviceImpl("templates/service.impl")
						.controller("templates/action")
				)
				.templateEngine(new FreemarkerTemplateEngine());

		autoGenerator.execute();
		try {
			java.awt.Desktop.getDesktop().open(new File(config.getOutputDir()));
		} catch (IOException e) {
			log.error("打开文件夹[" + config.getOutputDir() + "]失败", e);
		}
	}

	@Nonnull
	public static Properties getLocalProperties() {
		final Properties cfg;
		try {
			cfg = PropertiesLoaderUtils.loadAllProperties("mybatis-plus-generator.properties");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return cfg;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	public static class GeneratorConfig {

		@Nullable
		DataSource dataSource;
		DataSourceProperties ds;

		String outputDir = "D:/gencode";
		String parentPackage = "com.candlk.webapp.user";

		private GeneratorConfig() {
		}

		@Nonnull
		public DataSourceProperties getDsProps() {
			if (ds == null) {
				ds = getDefaultDataSourceProperties();
			}
			return ds;
		}

		public static DataSourceProperties getDefaultDataSourceProperties() {
			DataSourceProperties ds = new DataSourceProperties();
			ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
			ds.setUrl("jdbc:mysql:///test?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=Asia/Shanghai");
			ds.setUsername("root");
			ds.setPassword("Bj@#2023");
			return ds;
		}

		public static GeneratorConfig get(@Nullable Properties cfg) {
			final Properties merged = getLocalProperties();
			if (cfg != null) {
				merged.putAll(cfg);
			}
			GeneratorConfig config = new GeneratorConfig();
			DataSourceProperties ds = config.getDsProps();
			X.use(merged.getProperty("jdbc-url"), ds::setUrl);
			X.use(merged.getProperty("username"), ds::setUsername);
			X.use(merged.getProperty("password"), ds::setPassword);

			X.use(merged.getProperty("outputDir"), config::setOutputDir);
			X.use(merged.getProperty("package.parent"), config::setParentPackage);
			return config;
		}

		public static GeneratorConfig get() {
			return get(null);
		}

	}

}
