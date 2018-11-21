package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.FileInfo;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class SoftwareDisk extends ContainersCapable<SoftwareDiskArea> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String writeable;

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean invalidateCache) {
		Set<Path> files = new HashSet<>();
		for (Path sRoot : getSoftware().getRoots(invalidateCache)) {
			Path candidate = sRoot.resolve(name + ".chd");
			if (Files.isReadable(candidate))
				files.add(candidate);
		}
		return files;
	}


	@NotNull
	public Software getSoftware() {
		return getParentNode().getParentNode().getParentNode();
	}

	public boolean isAvailable() {
		return isAvailable(false);
	}

	public boolean isAvailable(boolean invalidateCache) {
		return getAvailableContainers(invalidateCache).size() > 0;
	}

	public boolean isValid() {
		return isValid(false);
	}

	public boolean isValid(boolean invalidateCache) {
		Set<Path> files = getAvailableContainers(invalidateCache);
		if (files.isEmpty())
			return false;
		if (files.size() > 1) {
			System.err.println("WARNING multiple disk images detected in different rompaths:");
			for (Path f : files)
				System.err.println("\t" + f);
		}
		Mame mame = getMame();
		synchronized (this) {
			for (Path file : files) {
				FileInfo info = getFileInfo(file);
				if (info.sha1 == null) {
					System.out.println("Calculating sha1 of " + file);
					info.sha1 = mame.sha1(file);
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
