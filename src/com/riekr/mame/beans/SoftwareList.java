package com.riekr.mame.beans;

import com.riekr.mame.attrs.AvailabilityCapable;
import com.riekr.mame.attrs.ContainersCapable;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SoftwareList extends ContainersCapable<SoftwareLists> implements Serializable, AvailabilityCapable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String description;

	@XmlElement(name = "software")
	private List<Software> _softwares;

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache) {
		HashSet<Path> roots = null;
		for (Path romPath : getMame().getRomPath()) {
			Path candidate = romPath.resolve(name);
			if (Files.isDirectory(candidate)) {
				if (complete)
					(roots == null ? roots = new HashSet<>() : roots).add(candidate);
				else
					return Collections.singleton(candidate);
			}
		}
		return roots == null ? Collections.emptySet() : roots;
	}

	@Override
	public boolean knownDumpExists() {
		return true;
	}

	public Stream<Software> softwares() {
		return _softwares == null ? Stream.empty() : _softwares.stream();
	}

	public boolean isComplete() {
		return isComplete(false);
	}

	public boolean isComplete(boolean invalidateCache) {
		return softwares().allMatch(s -> s.isComplete(invalidateCache));
	}

	@Override
	public String toString() {
		return name + ": " + description;
	}
}
