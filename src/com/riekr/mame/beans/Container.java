package com.riekr.mame.beans;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Container<ComponentType> {

	public final @NotNull ComponentType comp;
	public final @NotNull Path          path;

	public Container(@NotNull ComponentType comp, @NotNull Path path) {
		this.comp = comp;
		this.path = path;
	}

	public ComponentType comp() {
		return comp;
	}

	public Path path() {
		return path;
	}
}
