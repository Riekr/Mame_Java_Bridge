package com.riekr.mame.attrs;

public interface Completable {

	default boolean isComplete() {
		return isComplete(false);
	}

	boolean isComplete(boolean invalidateCache);

}
