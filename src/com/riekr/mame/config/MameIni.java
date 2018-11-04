package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class MameIni {

	private final @NotNull File _iniFile;
	private final @NotNull Map<String, String> _config = new HashMap<>();

	public MameIni(File iniFile) {
		if (!iniFile.canRead())
			throw new MameException("Unable to read " + iniFile);
		System.out.println("Reading " + iniFile);
		_iniFile = iniFile;
		Matcher dec = Pattern.compile("^([^#]\\w+)\\s+(.+)$").matcher("");
		try (Stream<String> stream = Files.lines(iniFile.toPath())) {
			stream.map(String::trim).forEach(line -> {
				dec.reset(line);
				if (dec.matches())
					_config.put(dec.group(1), dec.group(2));
			});
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new MameException("IO error reading " + iniFile);
		}
	}

	public File getHomePath(@Nullable File basedir) {
		if (basedir == null)
			basedir = _iniFile.getParentFile();
		File homepath = new File(_config.get("homepath"));
		if (!homepath.isAbsolute())
			homepath = new File(basedir, homepath.toString());
		if (!homepath.isDirectory())
			throw new MameException("Invalid 'homepath' in " + _iniFile + " (" + homepath + ')');
		return homepath;
	}

	@NotNull
	private Set<File> toPath(@Nullable File basedir, @NotNull String key) {
		File homepath = getHomePath(basedir);
		Set<File> res = new HashSet<>();
		Pattern.compile("[;:]").splitAsStream(_config.get(key))
				.forEach(rompath -> {
					File rompathFile = new File(rompath);
					if (!rompathFile.isAbsolute())
						rompathFile = new File(homepath, rompath);
					if (rompathFile.isDirectory())
						res.add(rompathFile);
					else
						System.err.println("Invalid " + key + " entry " + rompathFile);
				});
		return res;
	}

	@NotNull
	public Set<File> getRomPath(@Nullable File basedir) {
		return toPath(basedir, "rompath");
	}

	@NotNull
	public Set<File> getSamplePath(@Nullable File basedir) {
		return toPath(basedir, "samplepath");
	}
}
