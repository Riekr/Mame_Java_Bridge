package com.riekr.mame.mixins;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.stream.Stream;

/**
 * To be used AFTER initial lightweight filtering and BEFORE heavyweight processing
 * only if you are sure processing can go multi thread.
 */
public class ParallelOptions {

	@CommandLine.Option(names = "--parallel", description = "Enable multi thread parallel processing")
	public boolean parallel;

	@NotNull
	public <T> Stream<T> parallelize(@NotNull Stream<T> s) {
		if (parallel && !s.isParallel())
			return s.parallel();
		return s;
	}

}
