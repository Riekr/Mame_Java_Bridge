package com.riekr.mame.attrs;

import com.riekr.mame.beans.Container;
import com.riekr.mame.tools.Mame;
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

public abstract class ContainersCapable<ParentType extends Serializable> extends MameXmlChildOf<ParentType> implements AvailabilityCapable {

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

	public static boolean validateSha1(ContainersCapable<?> cc, boolean invalidateCache, String expectedSha1) {
		Set<Path> files = cc.getAvailableContainers(invalidateCache);
		if (files.isEmpty())
			return false;
		if (files.size() > 1) {
			System.err.println("WARNING multiple disk images detected in different rompaths:");
			for (Path f : files)
				System.err.println("\t" + f);
		}
		Mame mame = cc.getMame();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (cc) {
			for (Path file : files) {
				FileInfo info = cc.getFileInfo(file);
				if (info.sha1 == null) {
					System.out.println("Calculating sha1 of " + file.normalize());
					info.sha1 = mame.sha1(file);
					cc.notifyCachedDataChanged();
					if (!info.sha1.equalsIgnoreCase(expectedSha1)) {
						System.err.println("SHA1 of " + file + " mismatch:");
						System.err.println("\t" + expectedSha1 + " (mame)");
						System.err.println("\t" + info.sha1 + " (file)");
						return false;
					}
				}
			}
		}
		return true;
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

	@Override
	public final boolean isAvailable(boolean invalidateCache) {
		return getAvailableContainers(invalidateCache).size() > 0;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		SerUtils.writePaths(out, _containersInfo);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		_containersInfo = SerUtils.readPathMap(in);
	}
}
