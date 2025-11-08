package com.bojiu.context.model;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import com.bojiu.common.util.Common;
import me.codeplayer.util.StringUtil;

/**
 * 忽略 top_user_id 拼接
 */
public interface IgnoreTopAgent {

	static List<Long> splitChain(@Nullable String chain) {
		return StringUtil.notEmpty(chain) ? Common.splitAsLongList(chain) : Collections.emptyList();
	}

}