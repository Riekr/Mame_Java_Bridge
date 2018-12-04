package com.riekr.mame.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class Sync {

	public static void dcInit(@NotNull Object sync, @NotNull BooleanSupplier cond, @NotNull Runnable init) {
		if (cond.getAsBoolean()) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (sync) {
				if (cond.getAsBoolean())
					init.run();
			}
		}
	}

}
