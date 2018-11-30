package com.riekr.mame.beans;

import com.riekr.mame.attrs.ContainersCapable;
import com.riekr.mame.attrs.Validable;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SoftwareDisk extends ContainersCapable<SoftwareDiskArea> implements Serializable, Validable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String writeable;

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache) {
		Set<Path> files = null;
		for (Path sRoot : getSoftware().getRoots(invalidateCache)) {
			Path candidate = sRoot.resolve(name + ".chd");
			if (Files.isReadable(candidate)) {
				if (complete)
					(files == null ? files = new HashSet<>() : files).add(candidate);
				else
					return Collections.singleton(candidate);
			}
		}
		return files == null ? Collections.emptySet() : files;
	}

	@Override
	public boolean knownDumpExists() {
		return true;
	}

	@NotNull
	public Software getSoftware() {
		return getParentNode().getParentNode().getParentNode();
	}

	@Override
	public boolean isValid(boolean invalidateCache) {
		return validateSha1(invalidateCache, sha1);
	}
}
