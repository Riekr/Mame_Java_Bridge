package com.riekr.mame.xmlsource;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlSourceRef implements Serializable {

	@NotNull
	public static XmlSourceRef from(Path path) {
		return from(path.toFile());
	}

	@NotNull
	public static XmlSourceRef from(File file) {
		String ext = file.getName();
		int i = ext.lastIndexOf('.');
		if (i != -1) {
			ext = ext.substring(i + 1).toLowerCase();
			switch (ext) {
				case "exe":
					return new MameXmlSourceRef(file);
				case "gz":
					return new GZIPXmlSourceRef(file);
			}
		}
		return new XmlSourceRef(file);
	}

	public enum Type {

		MACHINES("-listxml"),
		SOFTWARES("-getsoftlist");

		public final String mameParam;

		Type(String mameParam) {
			this.mameParam = mameParam;
		}
	}

	protected @NotNull File _file;
	private            long _lastModified;

	XmlSourceRef(@NotNull File file) {
		_file = file;
		_lastModified = file.lastModified();
	}

	public boolean isOutDated() {
		return _file.lastModified() != _lastModified;
	}

	@NotNull
	public InputStream newInputStream(@NotNull Type type) throws IOException {
		return Files.newInputStream(_file.toPath());
	}
}
