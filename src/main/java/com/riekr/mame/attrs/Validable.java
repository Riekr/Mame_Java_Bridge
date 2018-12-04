package com.riekr.mame.attrs;

public interface Validable {

	boolean isValid(boolean invalidateCache);

	default boolean isValid() {
		return isValid(false);
	}

	default boolean isNotValid() {
		return !isValid(false);
	}

	default boolean isNotValid(boolean invalidateCache) {
		return !isValid(invalidateCache);
	}
}
