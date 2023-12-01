package com.candlk.common.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 【Spring依赖测试】基于 Spring ( Boot ) 运行环境的测试基类
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ "local", "unitTest" })
public abstract class SpringTest {

}
