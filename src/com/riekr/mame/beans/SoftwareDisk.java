package com.riekr.mame.beans;

import com.riekr.mame.tools.ChdMan;
import com.riekr.mame.utils.FileInfo;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoftwareDisk extends MameXmlChildOf<SoftwareDiskArea> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String writeable;

	private                    Map<Path, FileInfo> _filesInfo;
	private transient volatile Set<Path>           _files;

	@NotNull
	public Set<Path> getFiles() {
		return getFiles(false);
	}

	@NotNull
	public Set<Path> getFiles(boolean invalidateCache) {
		Sync.condInit(this, () -> invalidateCache || _files == null, () -> {
			_files = new HashSet<>();
			for (Path sRoot : getSoftware().getRoots(invalidateCache)) {
				Path candidate = sRoot.resolve(name + ".chd");
				if (Files.isReadable(candidate))
					_files.add(candidate);
			}
			if (_filesInfo != null && _filesInfo.keySet().retainAll(_files))
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
		Set<Path> files = getFiles(invalidateCache);
		if (files.isEmpty())
			return false;
		if (files.size() > 1) {
			System.err.println("WARNING multiple disk images detected in different rompaths:");
			for (Path f : files)
				System.err.println("\t" + f);
		}
		synchronized (this) {
			if (_filesInfo == null)
				_filesInfo = new HashMap<>();
			for (Path file : files) {
				FileInfo info = _filesInfo.computeIfAbsent(file, k -> new FileInfo());
				long lastModified = file.toFile().lastModified();
				if (info.sha1 == null || lastModified != info.lastModified) {
					info.lastModified = lastModified;
					System.out.println("Calculating sha1 of " + file);
					info.sha1 = ChdMan.sha1(file);
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
