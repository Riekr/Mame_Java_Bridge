package com.riekr.mame.attrs;

import com.riekr.mame.beans.Container;
import com.riekr.mame.utils.FileInfo;
import com.riekr.mame.tools.MameXmlChildOf;
import com.riekr.mame.utils.SerUtils;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ContainersCapable<ParentType extends Serializable> extends MameXmlChildOf<ParentType> implements AvailabilityCapable {

	public static <T extends ContainersCapable> Stream<Container<T>> unfold(@NotNull T containerCapable) {
		return unfold(containerCapable, false);
	}

	public static <T extends ContainersCapable<?>> Stream<Container<T>> unfold(@NotNull T containerCapable, boolean invalidateCache) {
		return containerCapable.availableContainers(true, invalidateCache)
				.map(path -> new Container<>(containerCapable, path));
	}

	private volatile Map<Path, FileInfo> _containersInfo;
	private transient volatile Set<Path> _containers;
	private transient boolean _containersAreComplete;

	@NotNull
	protected abstract Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache);

	@NotNull
	protected FileInfo getFileInfo(@NotNull Path path) {
		Sync.dcInit(this, () -> _containersInfo == null, ()
				-> _containersInfo = Collections.synchronizedMap(new HashMap<>()));
		FileInfo res = _containersInfo.computeIfAbsent(path, k -> {
			notifyCachedDataChanged();
			return new FileInfo();
		});
		if (res.update(path))
			notifyCachedDataChanged();
		return res;
	}


	public final Stream<Path> availableContainers() {
		return availableContainers(false);
	}

	public final Stream<Path> availableContainers(boolean complete) {
		return availableContainers(complete, false);
	}

	@NotNull
	public final Stream<Path> availableContainers(boolean complete, boolean invalidateCache) {
		Sync.dcInit(this, () -> _containers == null || invalidateCache || (!_containersAreComplete && complete), () -> {
			_containers = Collections.unmodifiableSet(getAvailableContainersImpl(complete, invalidateCache));
			_containersAreComplete = complete;
			if (_containersInfo != null && _containersInfo.keySet().retainAll(_containers))
				notifyCachedDataChanged();
		});
		return _containers.stream();
	}

	@Override
	public final boolean isAvailable(boolean invalidateCache) {
		return availableContainers(false, invalidateCache).findAny().isPresent();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		SerUtils.writePaths(out, _containersInfo);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		_containersInfo = SerUtils.readPathMap(in);
	}

	public abstract boolean knownDumpExists();

	protected synchronized final boolean validateSha1(boolean invalidateCache, String expectedSha1) {
		return availableContainers(true, invalidateCache)
				.allMatch(file -> {
					FileInfo info = getFileInfo(file);
					if (info.sha1 == null || invalidateCache) {
						System.out.println("Calculating sha1 of " + file.normalize());
						info.sha1 = getMame().sha1(file);
						notifyCachedDataChanged();
						if (!info.sha1.equalsIgnoreCase(expectedSha1)) {
							System.err.println("SHA1 of " + file + " mismatch:");
							System.err.println("\t" + expectedSha1 + " (mame)");
							System.err.println("\t" + info.sha1 + " (file)");
							return false;
						}
					}
					return true;
				});
	}
}
