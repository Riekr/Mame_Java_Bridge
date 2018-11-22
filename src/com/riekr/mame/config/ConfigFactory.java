package com.riekr.mame.config;

import com.riekr.mame.utils.CLIUtils;
import com.riekr.mame.xmlsource.XmlSourceRef;
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
	public Path mameExec;

	@CommandLine.Option(names = "--chdman", description = "ChdMan executable")
	public Path chdManExec;

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

	@CommandLine.Option(names = "--xml-machines", description = "Force machines xml to be read")
	public Path machinesXml;

	@CommandLine.Option(names = "--xml-softwares", description = "Force softwares xml to be read")
	public Path softwaresXml;

	@CommandLine.Option(names = "--cache-home", description = "Where to store cache files")
	public Path cacheHome;

	@Nullable
	private Path findExecInPath(@Nullable Path specifiedName, String... otherNames) {
		String[] names;
		if (specifiedName == null) {
			String ext = "";
			if (System.getProperty("os.name").toLowerCase().contains("win"))
				ext = ".exe";
			names = new String[otherNames.length];
			for (int i = 0; i < names.length; i++)
				names[i] = otherNames[i] + ext;
		} else
			names = new String[]{specifiedName.getFileName().toString()};
		Path exec = CLIUtils.findExecInPath(names);
		if (exec == null && baseDir != null) {
			for (String name : names) {
				exec = baseDir.resolve(name);
				if (Files.isExecutable(exec))
					return exec;
			}
		}
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
		if (mameExec == null || mameExec.getNameCount() == 1)
			mameExec = findExecInPath(mameExec, "mame", "mame64");
		if (baseDir == null && mameExec != null)
			baseDir = mameExec.getParent();
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
		if (chdManExec == null || chdManExec.getNameCount() == 1) {
			chdManExec = findExecInPath(chdManExec, "chdman");
		}
		// apply config
		MameConfig mameConfig = new MameConfig();
		if (cacheId != null) {
			Path home = cacheHome != null ? cacheHome : Path.of(System.getProperty("user.home"), ".com.riekr.mame");
			mameConfig.cacheFile = home.resolve(cacheId + ".cache");
		}
		mameConfig.mameExec = mameExec;
		mameConfig.chdManExec = chdManExec;
		mameConfig.romPath = romPath;
		mameConfig.samplePath = samplePath;
		if (machinesXml != null)
			mameConfig.machinesXmlRef = XmlSourceRef.from(machinesXml);
		if (softwaresXml != null)
			mameConfig.softwaresXmlRef = XmlSourceRef.from(softwaresXml);
		return mameConfig;
	}
}
