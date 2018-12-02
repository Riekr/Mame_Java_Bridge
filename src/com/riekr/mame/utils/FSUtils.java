package com.riekr.mame.utils;

import com.riekr.mame.tools.MameException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class FSUtils {

	private FSUtils() {
	}

	private static final LinkedHashMap<Path, Set<String>> _cache = new LinkedHashMap<>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > 1000;
		}
	};

	public static String toDotExt(String ext) {
		if (ext == null || ext.isBlank())
			return "";
		ext = ext.trim();
		if (ext.charAt(0) == '.')
			return ext;
		return '.' + ext;
	}

	public static Stream<Path> search(Path path, String name, boolean complete, boolean invalidateCache, String... extensions) {
		List<Path> res = null;
		for (String ext : extensions) {
			String currName = name + toDotExt(ext);
			if (contains(path, currName, invalidateCache)) {
				Path found = path.resolve(currName);
				if (complete)
					(res == null ? res = new ArrayList<>() : res).add(found);
				else
					return Stream.of(found);
			}
		}
		return res == null ? Stream.empty() : res.stream();
	}

	public static boolean contains(Path path, String name, boolean invalidateCache) {
		return contains(path, name, invalidateCache,
				// to avoid recursion greater than first level:
				p -> contains(p.getParent(), p.getFileName().toString(), invalidateCache, Files::exists));
	}

	private static boolean contains(Path path, String name, boolean invalidateCache, Predicate<Path> exists) {
		Set<String> names;
		synchronized (_cache) {
			names = _cache.get(path);
			if (names == null || invalidateCache) {
				if (exists.test(path)) {
					final HashSet<String> res = new HashSet<>();
					if (path.getFileName().toString().toLowerCase().endsWith(".zip")) {
						try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
							for (Path root : fs.getRootDirectories())
								list(root, res);
						} catch (IOException e) {
							throw new MameException("Unable to read " + path, e);
						}
					} else
						list(path, res);
					names = res;
				} else
					names = Collections.emptySet();
			}
			_cache.put(path, names); // refresh cache
		}
		return names.contains(name);
	}

	private static void list(Path container, Set<String> res) {
		try {
			Files.list(container).forEach(f -> res.add(f.getFileName().toString()));
		} catch (IOException e) {
			throw new MameException("Unable to list contents of " + container, e);
		}
	}

	public static void cleanCaches() {
		synchronized (_cache) {
			_cache.clear();
		}
	}
}
