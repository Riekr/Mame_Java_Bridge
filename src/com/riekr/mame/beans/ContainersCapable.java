package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ContainersCapable<ParentType extends Serializable> extends MameXmlChildOf<ParentType> {

	private transient volatile Set<Path> _containers;

	@NotNull
	protected abstract Set<Path> getAvailableContainersImpl();

	@NotNull
	public final Set<Path> getAvailableContainers() {
		return getAvailableContainers(false);
	}

	public final Set<Path> getAvailableContainers(boolean invalidateCache) {
		Sync.condInit(this, () -> _containers == null || invalidateCache, ()
				-> _containers = Collections.unmodifiableSet(getAvailableContainersImpl()));
		return _containers;
	}

	public final Stream<Path> availableContainers() {
		return availableContainers(false);
	}

	@NotNull
	public final Stream<Path> availableContainers(boolean invalidateCache) {
		return getAvailableContainers(invalidateCache).stream();
	}

	public final boolean haNoAvailableContainers() {
		return haNoAvailableContainers(false);
	}

	public final boolean haNoAvailableContainers(boolean invalidateCache) {
		return getAvailableContainers(invalidateCache).isEmpty();
	}

	public static <T extends ContainersCapable> Stream<Container<T>> unfold(@NotNull T containerCapable) {
		return unfold(containerCapable, false);
	}

	public static <T extends ContainersCapable<?>> Stream<Container<T>> unfold(@NotNull T containerCapable, boolean invalidateCache) {
		Set<Path> paths = containerCapable.getAvailableContainers(invalidateCache);
		if (paths.isEmpty())
			return Stream.empty();
		ArrayList<Container<T>> res = new ArrayList<>(paths.size());
		for (Path path : paths)
			res.add(new Container<>(containerCapable, path));
		return res.stream();
	}
}
