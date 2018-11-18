package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

public class ConfigFactory implements Supplier<MameConfig> {

	@CommandLine.Option(names = "--mame", description = "Mame executable")
	public File exec;

	@CommandLine.Option(names = "--rompath", split = ":;", description = "Paths to search for machines")
	public Set<File> romPath;

	@CommandLine.Option(names = "--samplepath", split = ":;", description = "Paths to search for samples")
	public Set<File> samplePath;

	@CommandLine.Option(names = "--mame-ini", description = "Mame.ini to load configurations from")
	public File ini;

	@CommandLine.Option(names = "--mame-dir", description = "Mame base directory for relative paths")
	public File baseDir;

	@CommandLine.Option(names = "--cache-id", defaultValue = "Mame", description = "Local mame cache id")
	public String cacheId;

	@Override
	public MameConfig get() {
		if (exec == null)
			exec = findMameExecInPath();
		if (baseDir == null)
			baseDir = exec.getParentFile();
		if (ini == null) {
			ini = new File(baseDir, "mame.ini");
			if (!ini.isFile())
				ini = null;
		}
		MameIni mameIni = ini == null ? null : new MameIni(ini);
		if (romPath == null) {
			if (mameIni != null)
				romPath = mameIni.getRomPath(baseDir);
			else
				romPath = searchPaths(baseDir, "roms", "SL", "CHD");
		}
		if (samplePath == null) {
			if (mameIni != null)
				samplePath = mameIni.getSamplePath(baseDir);
			else
				samplePath = searchPaths(baseDir, "samples");
		}
		return new MameConfig(exec, romPath, cacheId);
	}

	@NotNull
	private static File findMameExecInPath() {
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
				if (execFile.canExecute())
					return execFile;
			}
		}
		throw new MameException("Mame executable not found in path");
	}

	@NotNull
	private static Set<File> searchPaths(File baseDir, String... paths) {
		Set<File> romPath = new HashSet<>();
		for (String s : paths) {
			File candidate = new File(baseDir, s);
			if (candidate.isDirectory())
				romPath.add(candidate);
		}
		return romPath;
	}
}
