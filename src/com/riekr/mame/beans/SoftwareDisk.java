package com.riekr.mame.beans;

import com.riekr.mame.tools.ChdMan;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.FileInfo;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.File;
import java.io.Serializable;
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

	private Map<File, FileInfo> _filesInfo;
	private transient volatile Set<File> _files;

	@NotNull
	public Set<File> getFiles() {
		return getFiles(false);
	}

	@NotNull
	public Set<File> getFiles(boolean invalidateCache) {
		Sync.condInit(this, () -> invalidateCache || _files == null, () -> {
			_files = new HashSet<>();
			for (File sRoot : getSoftware().getRoots(invalidateCache)) {
				File candidate = new File(sRoot, name + ".chd");
				if (candidate.isFile())
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
		Set<File> files = getFiles(invalidateCache);
		if (files.isEmpty())
			return false;
		if (files.size() > 1) {
			System.err.println("WARNING multiple disk images detected in different rompaths:");
			for (File f : files)
				System.err.println("\t" + f);
		}
		synchronized (this) {
			if (_filesInfo == null)
				_filesInfo = new HashMap<>();
			for (File file : files) {
				FileInfo info = _filesInfo.get(file);
				if (info == null)
					_filesInfo.put(file, info = new FileInfo());
				if (info.sha1 == null || file.lastModified() != info.lastModified) {
					info.lastModified = file.lastModified();
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
