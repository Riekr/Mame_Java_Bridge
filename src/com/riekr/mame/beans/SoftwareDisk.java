package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.FileInfo;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SoftwareDisk extends MameXmlChildOf<SoftwareDiskArea> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String writeable;

	private Map<File, FileInfo> _filesInfo;
	private transient volatile Set<File> _files;

	@NotNull
	public Set<File> getFiles() {
		return getFiles(false);
	}

	@NotNull
	public Set<File> getFiles(boolean invalidateCache) {
		Sync.condInit(this, () -> invalidateCache || _files == null, () -> {
			Set<File> files = new HashSet<>();
			for (Path sRoot : getSoftware().getRoots(invalidateCache)) {
				Path candidate = sRoot.resolve(name + ".chd");
				if (Files.isReadable(candidate))
					files.add(candidate.toFile());
			}
			_files = Collections.unmodifiableSet(files);
			if (_filesInfo != null && _filesInfo.keySet().retainAll(files))
				notifyCachedDataChanged();
		});
		return _files;
	}


	@NotNull
	public Software getSoftware() {
		return getParentNode().getParentNode().getParentNode();
	}

	public boolean isAvailable() {
		return isAvailable(false);
	}

	public boolean isAvailable(boolean invalidateCache) {
		return getFiles(invalidateCache).size() > 0;
	}

	public boolean isValid() {
		return isValid(false);
	}

	public boolean isValid(boolean invalidateCache) {
		Set<File> files = getFiles(invalidateCache);
		if (files.isEmpty())
			return false;
		if (files.size() > 1) {
			System.err.println("WARNING multiple disk images detected in different rompaths:");
			for (File f : files)
				System.err.println("\t" + f);
		}
		Mame mame = getMame();
		synchronized (this) {
			if (_filesInfo == null)
				_filesInfo = new HashMap<>();
			for (File file : files) {
				FileInfo info = _filesInfo.computeIfAbsent(file, k -> new FileInfo());
				long lastModified = file.lastModified();
				if (info.sha1 == null || lastModified != info.lastModified) {
					info.lastModified = lastModified;
					System.out.println("Calculating sha1 of " + file);
					info.sha1 = mame.sha1(file.toPath());
					notifyCachedDataChanged();
					if (!info.sha1.equalsIgnoreCase(sha1)) {
						System.err.println("SHA1 of " + file + " mismatch:");
						System.err.println("\t" + sha1 + " (mame)");
						System.err.println("\t" + info.sha1 + " (file)");
						return false;
					}
				}
			}
		}
		return true;
	}
}
