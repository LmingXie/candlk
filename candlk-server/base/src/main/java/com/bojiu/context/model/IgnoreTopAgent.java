package com.bojiu.context.model;

import java.util.List;
import javax.annotation.Nullable;

import me.codeplayer.util.StringUtil;

/**
 * 忽略 top_user_id 拼接
 */
public interface IgnoreTopAgent {

	static List<Long> splitChain(@Nullable String chain) {
		return StringUtil.splitAsLongList(chain);
	}

}