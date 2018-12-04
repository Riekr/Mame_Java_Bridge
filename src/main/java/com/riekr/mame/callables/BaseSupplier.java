package com.riekr.mame.callables;

import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class BaseSupplier<T extends Stream> implements Supplier<T> {

	protected final @NotNull Supplier<Mame> _mame;

	public BaseSupplier(@NotNull Supplier<Mame> mame) {
		_mame = mame;
	}
}
