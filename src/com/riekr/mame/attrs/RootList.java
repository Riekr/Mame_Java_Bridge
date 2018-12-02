package com.riekr.mame.attrs;

import java.util.stream.Stream;

public interface RootList<T> {

	int count();

	Stream<T> all();

}
