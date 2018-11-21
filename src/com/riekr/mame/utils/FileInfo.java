package com.riekr.mame.utils;

import java.io.Serializable;
import java.nio.file.Path;

public class FileInfo implements Serializable {

	public long lastModified;
	public String sha1;

	public boolean update(Path path) {
		final long lastModified = path.toFile().lastModified();
		if (this.lastModified != lastModified) {
			this.sha1 = null;
			this.lastModified = lastModified;
			return true;
		}
		return false;
	}
}
