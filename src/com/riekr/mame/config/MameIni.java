package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class MameIni {

	private final @NotNull Path                _iniFile;
	private final @NotNull Map<String, String> _config = new HashMap<>();

	public MameIni(@NotNull Path iniFile) {
		System.out.println("Reading " + iniFile);
		_iniFile = iniFile;
		Matcher dec = Pattern.compile("^([^#]\\w+)\\s+(.+)$").matcher("");
		try (Stream<String> stream = Files.lines(iniFile)) {
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

	public Path getHomePath(@Nullable Path basedir) {
		if (basedir == null)
			basedir = _iniFile.getParent();
		Path homepath = basedir.resolve(_config.get("homepath"));
		if (!Files.isDirectory(homepath))
			throw new MameException("Invalid 'homepath' in " + _iniFile + " (" + homepath + ')');
		return homepath;
	}

	@NotNull
	private Set<Path> toPath(@Nullable Path basedir, @NotNull String key) {
		Path homepath = getHomePath(basedir);
		Set<Path> res = new HashSet<>();
		Pattern.compile("[;:]").splitAsStream(_config.get(key))
				.forEach(rompath -> {
					Path rompathFile = homepath.resolve(rompath);
					if (Files.isDirectory(rompathFile))
						res.add(rompathFile);
					else
						System.err.println("Invalid " + key + " entry " + rompathFile);
				});
		return res;
	}

	@NotNull
	public Set<Path> getRomPath(@Nullable Path basedir) {
		return toPath(basedir, "rompath");
	}

	@NotNull
	public Set<Path> getSamplePath(@Nullable Path basedir) {
		return toPath(basedir, "samplepath");
	}
}
