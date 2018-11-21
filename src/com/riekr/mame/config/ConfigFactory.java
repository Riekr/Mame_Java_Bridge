package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigFactory implements Supplier<MameConfig> {

	@CommandLine.Option(names = "--mame", description = "Mame executable")
	public Path exec;

	@CommandLine.Option(names = "--rompath", split = ":;", description = "Paths to search for machines")
	public Set<Path> romPath;

	@CommandLine.Option(names = "--samplepath", split = ":;", description = "Paths to search for samples")
	public Set<Path> samplePath;

	@CommandLine.Option(names = "--mame-ini", description = "Mame.ini to load configurations from")
	public Path ini;

	@CommandLine.Option(names = "--mame-dir", description = "Mame base directory for relative paths")
	public Path baseDir;

	@CommandLine.Option(names = "--cache-id", defaultValue = "Mame", description = "Local mame cache id")
	public String cacheId;

	@NotNull
	private static Path findMameExecInPath(@Nullable Path name) {
		String[] names;
		if (name == null) {
			String ext = "";
			if (System.getProperty("os.name").toLowerCase().contains("win"))
				ext = ".exe";
			names = new String[]{
					"mame" + ext,
					"mame64" + ext
			};
		} else
			names = new String[]{name.getFileName().toString()};
		Path exec = CLIUtils.findExecInPath(names);
		if (exec == null)
			throw new MameException("Mame executable not found in path");
		return exec;
	}

	@NotNull
	private static Set<Path> searchPaths(Path baseDir, String... paths) {
		Set<Path> romPath = new HashSet<>();
		for (String s : paths) {
			Path candidate = baseDir.resolve(s);
			if (Files.isDirectory(candidate))
				romPath.add(candidate);
		}
		return romPath;
	}

	@Override
	public MameConfig get() {
		if (exec == null || exec.getNameCount() == 1)
			exec = findMameExecInPath(exec);
		if (baseDir == null)
			baseDir = exec.getParent();
		if (ini == null && baseDir != null) {
			ini = baseDir.resolve("mame.ini");
			if (!Files.isReadable(ini))
				ini = null;
		}
		MameIni mameIni = ini == null ? null : new MameIni(ini);
		if (romPath == null) {
			if (mameIni != null)
				romPath = mameIni.getRomPath(baseDir);
			else if (baseDir != null)
				romPath = searchPaths(baseDir, "roms", "SL", "CHD");
		}
		if (samplePath == null) {
			if (mameIni != null)
				samplePath = mameIni.getSamplePath(baseDir);
			else if (baseDir != null)
				samplePath = searchPaths(baseDir, "samples");
		}
		return new MameConfig(exec, romPath, cacheId);
	}
}
