package com.riekr.mame.tools;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ChdMan {

	private static final Path _exec;

	static {
		// TODO make configurable
		_exec = Path.of(System.getProperty("chdman.exe", "D:\\Giochi\\Mame\\chdman.exe"));
	}

	public static String sha1(@NotNull Path file) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(_exec + " info -i \"" + file + '"');
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					// System.err.println(line);
					if (line.startsWith("SHA1:"))
						return line.substring(5).trim();
				}
			}
			return "";
		} catch (IOException e) {
			throw new MameException("Unable to create chd sha1 of " + file, e);
		}
	}

	private ChdMan() {
	}
}
