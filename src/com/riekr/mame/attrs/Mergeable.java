package com.riekr.mame.attrs;

public interface Mergeable {

	default boolean mergeIntoParent(boolean dryRun) {
		return mergeIntoParent(dryRun, false);
	}

	boolean mergeIntoParent(boolean dryRun, boolean invalidateCache);
}
