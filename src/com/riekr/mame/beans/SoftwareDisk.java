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
import java.util.Iterator;
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
		Iterator<Path> i = getSoftware()
				.availableContainers(complete, invalidateCache)
				.map(sRoot -> sRoot.resolve(name + ".chd"))
				.filter(Files::exists)
				.iterator();
		if (!i.hasNext())
			return Collections.emptySet();
		if (complete)
			return Collections.singleton(i.next());
		Set<Path> files = new HashSet<>();
		do {
			files.add(i.next());
		} while (i.hasNext());
		return files;
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
