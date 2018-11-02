package com.riekr.mame.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class MameConfig implements Serializable {

	private static final String     _defaultCacheId = "Mame";
	private static       MameConfig _defaultConfig;

	public static void setDefault(MameConfig config) {
		_defaultConfig = config;
	}

	public static MameConfig getDefault() {
		if (_defaultConfig == null)
			_defaultConfig = tryDetermine();
		return _defaultConfig;
	}

	public static MameConfig tryDetermine() {
		List<String> searchPaths = new ArrayList<>();
		for (Map.Entry<String, String> e : System.getenv().entrySet()) {
			if (e.getKey().equalsIgnoreCase("PATH"))
				Collections.addAll(searchPaths, e.getValue().split("\\Q" + File.pathSeparatorChar + "\\E"));
		}
		String ext = "";
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			searchPaths.add("D:\\Giochi\\Mame");
			ext = ".exe";
		}
		for (String path : searchPaths) {
			File mameRoot = new File(path);
			if (!mameRoot.isDirectory())
				continue;
			for (String exec : new String[]{"mame", "mame64"}) {
				File execFile = new File(mameRoot, exec + ext);
				if (!execFile.canExecute())
					continue;
				Set<File> romPath = new HashSet<>();
				// TODO search .ini
				for (String s : new String[]{"roms", "SL", "CHD"}) {
					File candidate = new File(mameRoot, s);
					if (candidate.isDirectory())
						romPath.add(candidate);
				}
				return new MameConfig(execFile, romPath, _defaultCacheId);
			}
		}
		return null;
	}

	public final @NotNull  File      exec;
	public final @NotNull  Set<File> romPath;
	public final @Nullable File      cacheFile;


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
