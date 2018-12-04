package com.riekr.mame.attrs;

public interface AvailabilityCapable {

	default boolean isAvailable() {
		return isAvailable(false);
	}

	boolean isAvailable(boolean invalidateCache);

	default boolean isNotAvailable() {
		return !isAvailable(false);
	}

	default boolean isNotAvailable(boolean invalidateCache) {
		return !isAvailable(invalidateCache);
	}

}
