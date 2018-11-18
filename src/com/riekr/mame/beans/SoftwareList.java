package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SoftwareList extends MameXmlChildOf<SoftwareLists> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String description;

	@XmlElement(name = "software")
	private List<Software> _softwares;

	private transient volatile Set<File> _roots;

	@Override
	public void setParentNode(@NotNull SoftwareLists parentNode) {
		super.setParentNode(parentNode);
		if (_softwares != null) {
			for (Software s : _softwares)
				s.setParentNode(this);
		}
	}

	@NotNull
	public Set<File> getRoots() {
		return getRoots(false);
	}

	@NotNull
	public Set<File> getRoots(boolean invalidateCache) {
		Sync.condInit(this, () -> _roots == null || invalidateCache, () -> {
			_roots = new HashSet<>();
			for (File romPath : Mame.getInstance().getRomPath()) {
				File candidate = new File(romPath, name);
				if (candidate.isDirectory())
					_roots.add(candidate);
			}
		});
		return _roots;
	}

	public boolean isAvailable() {
		return isAvailable(false);
	}

	public boolean isAvailable(boolean invalidateCache) {
		return getRoots(invalidateCache).size() > 0;
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
