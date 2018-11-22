package com.riekr.mame.xmlsource;

import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MameXmlSourceRef extends XmlSourceRef {

	private transient String _currentVersion;
	private           String _storedVersion;

	MameXmlSourceRef(@NotNull File file) {
		super(file);
	}

	@Override
	public boolean isOutDated() {
		if (super.isOutDated()) {
			// if file is changed let's check version
			return !Objects.equals(_currentVersion, _storedVersion);
		}
		return false;
	}

	@NotNull
	private String getCurrentVersion() {
		Sync.condInit(this, () -> _currentVersion == null, () -> {
			_currentVersion = "<undef>";
			try {
				System.out.println("Getting mame version from " + _file);
				File home = _file.getParentFile();
				Process proc = new ProcessBuilder(_file.toString(), "-help")
						.directory(home)
						.start();
				// MAME v0.203 (mame0203)
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					Matcher m = Pattern.compile("MAME\\s+v([0-9.]+)\\s+.*").matcher("");
					String line;
					while ((line = reader.readLine()) != null) {
						m.reset(line);
						if (m.matches()) {
							_currentVersion = m.group(1);
							break;
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Unable to get mame version");
				e.printStackTrace(System.err);
			}
		});
		return _currentVersion;
	}

	@NotNull
	@Override
	public InputStream newInputStream(@NotNull Type type) throws IOException {
		final String typeDescr = type.toString().toLowerCase();
		System.out.println("Getting " + typeDescr + " for v" + getCurrentVersion());
		File home = _file.getParentFile();
		Process proc = new ProcessBuilder(_file.toString(), type.mameParam)
				.directory(home)
				.start();
		InputStream res = proc.getInputStream();
		_storedVersion = _currentVersion;
		return res;
	}
}
