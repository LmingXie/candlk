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

#### 常用 Redis 命令

```shell
DEL playLogSyncRelay
DEL userPlayInOuts
DEL upgrade:user
DEL userPlayIns-*
DEL merchantStageTotalWins
DEL merchantTypeWins
```

|                                                   池子                                                   |  算力  | 总量 |      加成       |   
  |:------------------------------------------------------------------------------------------------------:|:----:|:--:|:-------------:|  
|      [TOGETHER](https://app.xai.games/pool/0x0bb6dd508da137d0e0b7c0d26b4eca824530d854/summary)【变】      | 6.92 | ×3 | 644127 esXAI  |   
|    [LFG! Capital](https://app.xai.games/pool/0xd5ceb0064fa9ffa242eed01e6cfed49b77f1b272/summary)【变】    | 6.72 | ×3 | 643733 esXAI  |   
|       [ICOSharks](https://app.xai.games/pool/0x7b325548913a2eb6a1ce456e658dea1bb146055b/summary)       | 6.71 | ×2 | 112997 esXAI  |   
| [GodBlessChina Capital](https://app.xai.games/pool/0x32bb4731bf5e51a30cae398cee3abc0de061ff1e/summary) | 6.39 | ×3 | 703752 esXAI  |   
|       [Arbitrum](https://app.xai.games/pool/0xe514bbce56bb134bb88e95a33ec90e015940cd2f/summary)        | 5.97 | ×3 | 1057474 esXAI |   
|     [Great Pool](https://app.xai.games/pool/0x85343b66e70a24853083a1c15cea27685c927e6f/summary)【变】     | 5.75 | ×3 | 1455405 esXAI |   
|      [DOUBLETOP_2](https://app.xai.games/pool/0xb7ef6377c93d85abc6f4411378ca9426b5c0d4ce/summary)      | 5.71 | ×2 | 130817 esXAI  |   
|       [AquaGems](https://app.xai.games/pool/0xc4be9475778df18b6e2a5b9e74cb08a0dab54170/summary)        | 5.57 | ×3 | 512128 esXAI  |   
|      [DOUBLETOP_1](https://app.xai.games/pool/0xd54bb7666d1f54a45f8c5eca14dba2e962570900/summary)      | 5.42 | ×2 | 138315 esXAI  |   
|     [Sandbar🏝️](https://app.xai.games/pool/0xed03329e096d6532a81b48dced7b02d43aeb3cde/summary)【变】     | 5.26 | ×3 | 1029020 esXAI |   
|     [CryptoTelugu](https://app.xai.games/pool/0x85026431eab9ca0a99e5666729807208af02a69f/summary)      | 4.77 | ×3 | 1415797 esXAI |   
|       [XAI-POOL](https://app.xai.games/pool/0xbd2cf0ff096c8ad0218262573f368a849caa61e7/summary)        | 4.63 | ×2 | 116212 esXAI  |   
|      [XAiverse](https://app.xai.games/pool/0x6c8b53dab8c7cb2da60a450d7f4d47bca6f94b0e/summary)【变】      | 4.35 | ×3 | 2579334 esXAI |   
|        [Xaiborg](https://app.xai.games/pool/0x1d2d533751187a4ed0ec78502d6d71b0d646bcf2/summary)        | 4.26 | ×3 | 2183600 esXAI |   
|     [ARC community](https://app.xai.games/pool/0x4f72c3fb6d71f4be0b8a9ad592a272662168ab85/summary)     | 4.15 | ×2 | 240395 esXAI  |   
|       [ShanyiXAI](https://app.xai.games/pool/0x110a1c87d0ea90e166806ebe5284ea9dcc4a319a/summary)       | 3.99 | ×3 | 2671959 esXAI |   
|      [Game Theory](https://app.xai.games/pool/0x2f5d92d278d5fbde5c9d06dde38c9281cf19b319/summary)      | 3.70 | ×2 | 190156 esXAI  |   
|      [helloworld](https://app.xai.games/pool/0x8d83da8fd83c91525b50ec2753277e3aa591e879/summary)       | 3.53 | ×3 | 509764 esXAI  |   
|      [XMG Capital](https://app.xai.games/pool/0x95cd14fc5d5cce0c83bf266dda0ee8af8246711f/summary)      | 3.49 | ×3 | 3212629 esXAI |   
|     [冰蛙 \| IceFrog](https://app.xai.games/pool/0x507c9f6325fb8106f7c90bab31705300a018e340/summary)     | 3.44 | ×3 | 523511 esXAI  |      