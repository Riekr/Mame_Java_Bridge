package com.riekr.mame.attrs;

public interface Validable {

	boolean isValid(boolean invalidateCache);

	default boolean isValid() {
		return isValid(false);
	}
}
