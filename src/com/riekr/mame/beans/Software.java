package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Software extends MameXmlChildOf<SoftwareList> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String cloneof;

	@XmlElement
	public String description;

	@XmlElement
	public String year;

	@XmlElement
	public String publisher;

	@XmlElement(name = "info")
	private List<SoftwareInfo> _infos;

	@XmlElement(name = "part")
	private List<SoftwarePart> _parts;

	private transient Set<File> _roots;

	@Override
	public void setParentNode(@NotNull SoftwareList parentNode) {
		super.setParentNode(parentNode);
		if (_infos != null) {
			for (SoftwareInfo i : _infos)
				i.setParentNode(this);
		}
		if (_parts != null) {
			for (SoftwarePart p : _parts)
				p.setParentNode(this);
		}
	}

	@NotNull
	public Set<File> getRoots() {
		if (_roots == null) {
			_roots = new HashSet<>();
			for (File slRoot : getParentNode().getRoots()) {
				File candidate = new File(slRoot, name);
				if (candidate.isDirectory())
					_roots.add(candidate);
			}
		}
		return _roots;
	}

	public boolean isClone() {
		return cloneof != null;
	}

	public boolean isAvailable() {
		return getRoots().size() > 0;
	}

	public boolean isComplete() {
		return disks().allMatch(SoftwareDisk::isAvailable);
	}

	public boolean mergeIntoParent(boolean dryRun) {
		if (!isClone())
			throw new UnsupportedOperationException("Can't merge a non clone set");
		if (!isAvailable())
			throw new UnsupportedOperationException("Can't merge an unavailable set");
		System.out.println("Processing " + this);
		if (dryRun)
			return false;
		Set<File> roots = getRoots();
		if (roots.size() > 1)
			throw new UnsupportedOperationException("Multiple roots detected for " + this + " in " + roots);
		File root = roots.iterator().next();
		File parentDir = new File(root.getParent(), cloneof);
		if (!parentDir.isDirectory() && !parentDir.mkdir()) {
			System.err.println("Unable to create " + parentDir);
			return false;
		}
		File[] cloneFiles = root.listFiles();
		if (cloneFiles == null || cloneFiles.length == 0) {
			System.err.println("No files in " + root);
			return false;
		}
		int errors = 0, successes = 0;
		for (File srcFile : cloneFiles) {
			File destFile = new File(parentDir, srcFile.getName());
			if (destFile.exists() && !srcFile.delete()) {
				System.err.println("Unable to delete " + srcFile);
				errors++;
			}
			if (!srcFile.renameTo(destFile)) {
				System.err.println("Unable to move " + srcFile + " to " + parentDir);
				errors++;
			} else
				successes++;
		}
		if (errors == 0 && !root.delete())
			System.err.println("Unable to delete " + root);
		return successes > 0;
	}

	public Stream<SoftwarePart> parts() {
		return _parts == null ? Stream.empty() : _parts.stream();
	}

	public Stream<SoftwareDisk> disks() {
		return parts()
				.flatMap(SoftwarePart::diskAreas)
				.flatMap(SoftwareDiskArea::disks);
	}

	public Stream<Software> clones() {
		return getParentNode().softwares()
				.filter(s -> name.equals(s.cloneof));
	}

	public Software parent() {
		return getParentNode().softwares()
				.filter(s -> s.name.equals(cloneof))
				.findAny()
				.orElse(null);
	}

	@Override
	public String toString() {
		return getParentNode().name + '/' + name + ": " + description;
	}
}
