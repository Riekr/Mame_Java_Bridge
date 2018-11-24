package com.riekr.mame.attrs;

public interface Completable {

	default boolean isComplete() {
		return isComplete(false);
	}

	boolean isComplete(boolean invalidateCache);

	default boolean isNotComplete() {
		return !isComplete(false);
	}

	default boolean isNotComplete(boolean invalidateCache) {
		return !isComplete(invalidateCache);
	}
}
