package com.riekr.mame.attrs;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Searchable<Result> {

	@NotNull
	Stream<Result> search(@NotNull Stream<String> keys);

	@NotNull
	default Stream<Result> search(@NotNull Iterable<String> keys) {
		return search(StreamSupport.stream(keys.spliterator(), false));
	}

	@NotNull
	default Stream<Result> search(@NotNull String... key) {
		return search(Stream.of(key));
	}

}
