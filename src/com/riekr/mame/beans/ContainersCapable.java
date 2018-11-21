package com.riekr.mame.beans;

import com.riekr.mame.utils.FileInfo;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.SerUtils;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public abstract class ContainersCapable<ParentType extends Serializable> extends MameXmlChildOf<ParentType> {

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


	private Map<Path, FileInfo> _containersInfo;
	private transient volatile Set<Path> _containers;

	@NotNull
	protected abstract Set<Path> getAvailableContainersImpl(boolean invalidateCache);

	@NotNull
	protected FileInfo getFileInfo(@NotNull Path path) {
		Sync.condInit(this, () -> _containersInfo == null, ()
				-> _containersInfo = Collections.synchronizedMap(new HashMap<>()));
		FileInfo res = _containersInfo.computeIfAbsent(path, k -> {
			notifyCachedDataChanged();
			return new FileInfo();
		});
		if (res.update(path))
			notifyCachedDataChanged();
		return res;
	}

	@NotNull
	public final Set<Path> getAvailableContainers() {
		return getAvailableContainers(false);
	}

	public final Set<Path> getAvailableContainers(boolean invalidateCache) {
		Sync.condInit(this, () -> _containers == null || invalidateCache, () -> {
			_containers = Collections.unmodifiableSet(getAvailableContainersImpl(invalidateCache));
			if (_containersInfo != null && _containersInfo.keySet().retainAll(_containers))
				notifyCachedDataChanged();
		});
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

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		SerUtils.writePaths(out, _containersInfo);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		_containersInfo = SerUtils.readPathMap(in);
	}
}
