package com.riekr.mame.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class MameConfig implements Serializable {

	private static MameConfig _defaultConfig;

	public static MameConfig tryDetermine() {
		if (_defaultConfig == null) {
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
			mainLoop:
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
					_defaultConfig = new MameConfig(execFile, romPath, "Mame");
					break mainLoop;
				}
			}
		}
		return _defaultConfig;
	}

	public final @NotNull File exec;
	public final @NotNull Set<File> romPath;
	public final @Nullable File cacheFile;


	public MameConfig(@NotNull File exec, @NotNull Set<File> romPath, String id) {
		this.exec = exec;
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
