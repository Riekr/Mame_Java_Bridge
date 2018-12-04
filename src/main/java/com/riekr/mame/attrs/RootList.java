package com.riekr.mame.attrs;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface RootList<T> {

	int count();

	@NotNull
	Stream<T> all();

}
