package com.candlk.common.model;

/**
 * 标识当前实例具有业务标识字段
 */
public interface BizFlag {

	long getBizFlag();

	default boolean hasFlag(final long flag) {
		return hasFlag(getBizFlag(), flag);
	}

	default boolean hasFlag(BizFlag bizFlag) {
		return hasFlag(getBizFlag(), bizFlag.getBizFlag());
	}

	default boolean hasAnyFlag(final long... flags) {
		return hasAnyFlag(getBizFlag(), flags);
	}

	default boolean hasAnyFlag(final BizFlag... flagCells) {
		return hasAnyFlag(getBizFlag(), flagCells);
	}

	static boolean hasFlag(final long flags, final long flag) {
		return flag > 0 && (flags & flag) == flag;
	}

	static boolean hasFlag(final long flags, final BizFlag cell) {
		return hasFlag(flags, cell.getBizFlag());
	}

	static boolean hasFlag(final int flags, final int flag) {
		return flag > 0 && (flags & flag) == flag;
	}

	static boolean hasAnyFlag(final long container, final long... flags) {
		if (container > 0) {
			for (long flag : flags) {
				if (flag > 0 && (container & flag) == flag) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean hasAnyFlag(final long container, final BizFlag... flagCells) {
		if (container > 0) {
			for (BizFlag cell : flagCells) {
				final long flag = cell.getBizFlag();
				if (flag > 0 && (container & flag) == flag) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean hasAnyFlag(final int container, final int... flags) {
		if (container > 0) {
			for (long flag : flags) {
				if (flag > 0 && (container & flag) == flag) {
					return true;
				}
			}
		}
		return false;
	}

	interface WritableBizFlag extends BizFlag {

		void setBizFlag(long flag);

		default void addFlag(final long flag) {
			setBizFlag(getBizFlag() | flag);
		}

		static long toggleFlag(final long container, final long flag, boolean addOrRemove) {
			return addOrRemove ? container | flag : container & ~flag;
		}

		default void addFlag(final BizFlag flagCell) {
			addFlag(flagCell.getBizFlag());
		}

		default void removeFlag(final long flag) {
			setBizFlag(getBizFlag() & ~flag);
		}

		default void removeFlag(final BizFlag flagCell) {
			removeFlag(flagCell.getBizFlag());
		}

		default void toggleFlag(final long flag, boolean addOrRemove) {
			setBizFlag(toggleFlag(getBizFlag(), flag, addOrRemove));
		}

		default void toggleFlag(final BizFlag cell, boolean addOrRemove) {
			toggleFlag(cell.getBizFlag(), addOrRemove);
		}

		default void mergeDiff(long[] diffs, boolean addOrRemove) {
			setBizFlag(mergeDiff(getBizFlag(), diffs, addOrRemove));
		}

		static long mergeRange(final BizFlag... mergedRange) {
			long range = 0;
			for (BizFlag cell : mergedRange) {
				range |= cell.getBizFlag();
			}
			return range;
		}

		static long merge(final long oldBizFlag, final long mergedRange, final long replace) {
			return oldBizFlag & ~mergedRange | replace;
		}

		static long merge(final long oldBizFlag, final BizFlag[] mergedRange, final long replace) {
			return merge(oldBizFlag, mergeRange(mergedRange), replace);
		}

		static long mergeDiff(long container, long[] diffs, final boolean addOrRemove) {
			container = toggleFlag(container, diffs[0], addOrRemove);
			return toggleFlag(container, diffs[1], !addOrRemove);
		}

	}

}
