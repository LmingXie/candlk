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


|  排名  |  池子  |  算力  |  加成   | 总质押  | 活跃  |  
|:------:|:------:|:-------:|:-----:  |  :-----:  |  :-----:  |  
| 1 | [GreatPool](https://app.xai.games/pool/0x85343b66e70a24853083a1c15cea27685c927e6f/summary)<font color="red">【变】</font> | 6.73 | ×3 | 131w | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 2 | [Sandbar🏝️](https://app.xai.games/pool/0xed03329e096d6532a81b48dced7b02d43aeb3cde/summary) | 6.11 | ×2 | 23w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 3 | [LFG!Capita](https://app.xai.games/pool/0xd5ceb0064fa9ffa242eed01e6cfed49b77f1b272/summary)<font color="red">【变】</font> | 5.85 | ×3 | 57w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 4 | [NodeOps](https://app.xai.games/pool/0xeb27cab01c52b40ade3cd644f3ecf3cd7a0763b8/summary) | 5.63 | ×3 | 71w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 5 | [ICOSharks](https://app.xai.games/pool/0x7b325548913a2eb6a1ce456e658dea1bb146055b/summary) | 5.58 | ×2 | 14w | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 6 | [GodBlessCh](https://app.xai.games/pool/0x32bb4731bf5e51a30cae398cee3abc0de061ff1e/summary) | 5.49 | ×3 | 77w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 7 | [XAiverse](https://app.xai.games/pool/0x6c8b53dab8c7cb2da60a450d7f4d47bca6f94b0e/summary) | 5.34 | ×3 | 157w | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 8 | [unitycapit](https://app.xai.games/pool/0x80d1a3c84b7c7185dc7dbf4787713d55eea95e27/summary) | 5.07 | ×3 | 203w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 9 | [Arbitrum](https://app.xai.games/pool/0xe514bbce56bb134bb88e95a33ec90e015940cd2f/summary) | 5.05 | ×3 | 134w | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 10 | [TOGETHER](https://app.xai.games/pool/0x0bb6dd508da137d0e0b7c0d26b4eca824530d854/summary) | 5.02 | ×3 | 67w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 11 | [DOUBLETOP_](https://app.xai.games/pool/0xb7ef6377c93d85abc6f4411378ca9426b5c0d4ce/summary) | 4.99 | ×2 | 15w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 12 | [Keyholders](https://app.xai.games/pool/0xd5d93d86e6f0f4bd910014ac760bc3f031cea80b/summary)<font color="red">【变】</font> | 4.98 | ×3 | 62w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 13 | [CryptoTelu](https://app.xai.games/pool/0x85026431eab9ca0a99e5666729807208af02a69f/summary) | 4.76 | ×3 | 142w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 14 | [ARCcommuni](https://app.xai.games/pool/0x4f72c3fb6d71f4be0b8a9ad592a272662168ab85/summary) | 4.60 | ×2 | 21w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 15 | [Zeneca](https://app.xai.games/pool/0x59425e14f186619bec8ec0ffcfa8f48d72e3f641/summary) | 4.26 | ×2 | 24w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 16 | [DOUBLETOP_](https://app.xai.games/pool/0xd54bb7666d1f54a45f8c5eca14dba2e962570900/summary) | 4.24 | ×2 | 18w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 17 | [XAIPOOL](https://app.xai.games/pool/0xbd2cf0ff096c8ad0218262573f368a849caa61e7/summary) | 4.12 | ×2 | 11w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 18 | [AquaGems](https://app.xai.games/pool/0xc4be9475778df18b6e2a5b9e74cb08a0dab54170/summary)<font color="red">【变】</font> | 4.10 | ×3 | 91w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 19 | [LongXiaPoo](https://app.xai.games/pool/0x8e570e4e9819d26834ecc427c4a2b2a064e0af2f/summary)<font color="red">【变】</font> | 4.01 | ×3 | 67w | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 20 | [ShanyiXAI](https://app.xai.games/pool/0x110a1c87d0ea90e166806ebe5284ea9dcc4a319a/summary) | 4.01 | ×3 | 286w | <font color="red">[✘](https://arbiscan.io/address/null)</font> |


|  排名  |   池子   |   算力   |  加成  | 总质押  | 活跃  |
|:------:|:------:|:-------:|:-----:  |  :-----:  |  :-----:  |  
| 1 | [GCRClassic](https://app.xai.games/pool/0x958e5cc35fd7f95c135d55c7209fa972bdb68617/summary)<font color="red">【变】</font> | 3.60 | ×6 | 750 | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 2 | [elonmusk](https://app.xai.games/pool/0x124efad83c11cb1112a8a342e83233619b41a992/summary)<font color="red">【变】</font> | 3.00 | ×6 | 750 | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 3 | [XaiBhais](https://app.xai.games/pool/0x1d7725ca66c7eb4089c80d92876cb69558830367/summary)<font color="red">【变】</font> | 3.00 | ×6 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 4 | [VANPOXAI](https://app.xai.games/pool/0xa150d845a4dfadd6f4db9ec08370c79b989fbaa6/summary)<font color="red">【变】</font> | 2.87 | ×6 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 5 | [OnePiece](https://app.xai.games/pool/0xfad512e0de22f5ae4a873868af3abc9b763d9c50/summary)<font color="red">【变】</font> | 2.85 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 6 | [冰蛙IceFrog](https://app.xai.games/pool/0x507c9f6325fb8106f7c90bab31705300a018e340/summary) | 2.73 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 7 | [ALPHA1](https://app.xai.games/pool/0x081408b0075f2dec5bbb5b5d02e215992b3bd6cc/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 8 | [ALPHA3](https://app.xai.games/pool/0x717f8f704d599b6b80f16edd76be5b95b16e1027/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 9 | [冰蛙Ice_Frog](https://app.xai.games/pool/0x9e14d117079cdfee20fb5bf69a906fefaecc9e9c/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 10 | [冰蛙Ice_Frog](https://app.xai.games/pool/0x9e14d117079cdfee20fb5bf69a906fefaecc9e9c/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 11 | [ALPHA7Fran](https://app.xai.games/pool/0x8be447d88d8a7e4a2492c72048ce9a3e267ea5a2/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 12 | [ALPHA2](https://app.xai.games/pool/0x57c36988d0134b4998b1fda3a55fcabdbf348f42/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 13 | [ALPHA6](https://app.xai.games/pool/0x499d227eac69c5abb22f638721661d4b2fa19c7c/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 14 | [ALPHA5Fran](https://app.xai.games/pool/0x400ee9b0af9b946bef47e57d0b3fc19ecfc47120/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 15 | [XAI_fans](https://app.xai.games/pool/0x9a0aa81a7a6c0c82e72b91244bcab051033fa42a/summary) | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 16 | [helloworld](https://app.xai.games/pool/0x8d83da8fd83c91525b50ec2753277e3aa591e879/summary) | 2.70 | ×3 | 750 | <font color="red">[✘](https://arbiscan.io/address/null)</font> |   
| 17 | [ALPHA4](https://app.xai.games/pool/0x1a980607e8fb111c117a4fa95798baaf5c1674f5/summary)<font color="red">【变】</font> | 2.70 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 18 | [LongXiaPoo](https://app.xai.games/pool/0x8e570e4e9819d26834ecc427c4a2b2a064e0af2f/summary)<font color="red">【变】</font> | 2.55 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 19 | [OnlyKeysNe](https://app.xai.games/pool/0x01495a0783c6e011a9507b26acf35674e02d8860/summary) | 2.55 | ×3 | 593 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
| 20 | [TOGETHER](https://app.xai.games/pool/0x0bb6dd508da137d0e0b7c0d26b4eca824530d854/summary) | 2.43 | ×3 | 750 | <font color="common_green1_color">[✔️](https://arbiscan.io/address/null)</font> |   
  