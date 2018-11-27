package com.riekr.mame.utils;

import com.riekr.mame.tools.MameException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ZipUtils {

	private ZipUtils() {
	}

	private static final LinkedHashMap<Path, Set<String>> _cache = new LinkedHashMap<>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > 1000;
		}
	};

	public static boolean contains(Path zip, String name) {
		Set<String> names;
		synchronized (_cache) {
			names = _cache.get(zip);
			if (names == null) {
				if (Files.exists(zip)) {
					final HashSet<String> res = new HashSet<>();
					try (FileSystem fs = FileSystems.newFileSystem(zip, null)) {
						for (Path root : fs.getRootDirectories()) {
							try {
								Files.list(root).forEach(f -> res.add(f.getFileName().toString()));
							} catch (IOException e) {
								throw new MameException("Unable to list contents of " + zip, e);
							}
						}
					} catch (IOException e) {
						throw new MameException("Unable to read " + zip, e);
					}
					names = res;
				} else
					names = Collections.emptySet();
			}
			_cache.put(zip, names); // refresh cache
		}
		final boolean contains = names.contains(name);
		return contains;
	}

	public static void cleanCaches() {
		synchronized (_cache) {
			_cache.clear();
		}
	}
}
