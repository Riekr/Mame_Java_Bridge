package com.riekr.mame.utils;

import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.Nullable;

import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.CREATE;

public final class CacheFileManager extends Thread {

	private static volatile Map<Path, Mame>  _WRITES   = null;
	private static volatile Set<Path>        _REMOVALS = null;
	private static volatile CacheFileManager _MANAGER  = null;

	public static void register(Path cacheFile, Mame mame) {
		if (_WRITES == null) {
			synchronized (CacheFileManager.class) {
				if (_WRITES == null) {
					_WRITES = Collections.synchronizedMap(new HashMap<>());
					if (_MANAGER == null)
						Runtime.getRuntime().addShutdownHook(_MANAGER = new CacheFileManager());
				}
			}
		}
		Mame oldMame = _WRITES.put(cacheFile, mame);
		if (oldMame != null && oldMame != mame) {
			_WRITES.put(cacheFile, oldMame);
			throw new IllegalStateException("Another mame instance is already associated to cache file " + cacheFile);
		}
	}

	public static void invalidate(Path cacheFile) {
		if (_REMOVALS == null) {
			synchronized (CacheFileManager.class) {
				if (_REMOVALS == null) {
					_REMOVALS = Collections.synchronizedSet(new HashSet<>());
					if (_MANAGER == null)
						Runtime.getRuntime().addShutdownHook(_MANAGER = new CacheFileManager());
				}
			}
		}
		_REMOVALS.add(cacheFile);
	}

	private CacheFileManager() {
	}

	private static void writeCache(Path path, Mame mame) {
		System.out.println("Writing cache to " + path);
		try {
			Files.createDirectories(path.getParent());
			try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(path, CREATE)))) {
				oos.writeObject(mame);
			}
		} catch (Exception e) {
			System.err.println("Unable to write " + path);
			e.printStackTrace(System.err);
		}
	}

	public static void removeCache(Path path) {
		System.out.println("Deleting caches from " + path);
		try {
			if (Files.deleteIfExists(path)) {
				Path parent = path.getParent();
				if (Files.list(parent).noneMatch(p -> true)) {
					System.out.println("Deleting empty " + path);
					Files.delete(parent);
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to delete " + path);
			e.printStackTrace(System.err);
		}
	}

	@Nullable
	public static Mame loadCache(@Nullable Path cacheFile) {
		if (cacheFile == null || !Files.isReadable(cacheFile))
			return null;
		try {
			System.out.println("Loading caches from " + cacheFile);
			Mame mame;
			try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(Files.newInputStream(cacheFile)))) {
				mame = (Mame) ois.readObject();
			}
			if (mame != null) {
				System.out.println("Restored cache from " + cacheFile);
				return mame;
			}
		} catch (InvalidClassException e) {
			System.err.println("Cache format changed, data invalidated.");
		} catch (Exception e) {
			System.err.println("Unable to read " + cacheFile);
			e.printStackTrace(System.err);
		}
		return null;
	}


	@Override
	public void run() {
		if (_WRITES != null) {
			_WRITES.forEach((path, mame) -> {
				if (_REMOVALS != null && _REMOVALS.remove(path))
					removeCache(path);
				else
					writeCache(path, mame);
			});
		}
		if (_REMOVALS != null)
			_REMOVALS.forEach(CacheFileManager::removeCache);
	}
}
