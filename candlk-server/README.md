# Candlk

项目服务 公共依赖的父 POM

- Spring Cloud 2021.0.x
- Spring Cloud Alibaba 2021.0.x

### JDK 17 add-opens

```shell
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
--add-opens=java.base/java.math=ALL-UNNAMED
# 下面的一般用不到
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/java.nio=ALL-UNNAMED
--add-opens=java.base/java.security=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.base/java.time=ALL-UNNAMED
--add-opens=java.base/jdk.internal.access=ALL-UNNAMED
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED
--add-opens=java.sql/java.sql=ALL-UNNAMED
```

### HanLP 分词器

下载 HanLP 最新模型数据包（data-for-1.7.5.zip）：https://github.com/hankcs/HanLP/releases

解压缩后修改[hanlp.properties](condlk-user%2Fsrc%2Fmain%2Fresources%2Fhanlp.properties)中的`root``跟路径（使用绝对路径！）

### ES9.0 插件安装
```shell
# IK分词器 https://release.infinilabs.com/analysis-ik/stable/
elasticsearch-plugin install https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-9.0.0.zip

# 日语分词器
elasticsearch-plugin install analysis-nori
# 韩语分词器
elasticsearch-plugin install analysis-nori
```