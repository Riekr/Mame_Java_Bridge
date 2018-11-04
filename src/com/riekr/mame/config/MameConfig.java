package com.riekr.mame.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class MameConfig implements Serializable {

	public final @NotNull File exec;
	public final @NotNull Set<File> romPath;
	public final @Nullable File cacheFile;

	public MameConfig(@NotNull File exec, @Nullable Set<File> romPath, String id) {
		this.exec = exec;
		if (romPath == null || romPath.isEmpty())
			this.romPath = Collections.emptySet();
		else
			this.romPath = Collections.unmodifiableSet(romPath);
		if (id == null || id.isBlank())
			cacheFile = null;
		else
			cacheFile = new File(new File(System.getProperty("user.home"), ".com.riekr.mame"), id + ".cache");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MameConfig config = (MameConfig) o;
		return Objects.equals(exec, config.exec) && Objects.equals(romPath, config.romPath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(exec, romPath);
	}
}
