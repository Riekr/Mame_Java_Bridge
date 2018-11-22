package com.riekr.mame.beans;

import com.riekr.mame.attrs.AvailabilityCapable;
import com.riekr.mame.attrs.Completable;
import com.riekr.mame.attrs.Mergeable;
import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Software extends MameXmlChildOf<SoftwareList> implements Serializable, Mergeable, Completable, AvailabilityCapable {

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

	private volatile           Software  _parent;
	private transient volatile Set<Path> _roots;

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
	public Set<Path> getRoots() {
		return getRoots(false);
	}

	@NotNull
	public Set<Path> getRoots(boolean invalidateCache) {
		Sync.condInit(this, () -> _roots == null || invalidateCache, () -> {
			_roots = new HashSet<>();
			for (Path slRoot : getParentNode().getRoots(invalidateCache)) {
				Path candidate = slRoot.resolve(name);
				if (Files.isDirectory(candidate))
					_roots.add(candidate);
			}
		});
		return _roots;
	}

	@Nullable
	public Software getParent() {
		if (!isClone())
			return null;
		Sync.condInit(this, () -> _parent == null, () -> {
			SoftwareList sl = getParentNode();
			Optional<Software> parent = sl.softwares().filter(s -> s.name.equals(cloneof)).findFirst();
			if (parent.isPresent()) {
				_parent = parent.get();
				notifyCachedDataChanged();
			} else
				throw new MameException("Can't find parent software of '" + name + "' (" + cloneof + ')');
		});
		return _parent;
	}

	public boolean isClone() {
		return cloneof != null;
	}

	@Override
	public boolean isAvailable(boolean invalidateCache) {
		return getRoots(invalidateCache).size() > 0;
	}

	@Override
	public boolean isComplete(boolean invalidateCache) {
		return disks().allMatch(sd -> sd.isAvailable(invalidateCache));
	}

	@Override
	public boolean mergeIntoParent(boolean dryRun, boolean invalidateCache) {
		Software parent = getParent();
		if (parent == null)
			throw new MameException("Can't merge a non clone set (" + this + ')');
		if (!isAvailable(invalidateCache))
			throw new MameException("Can't merge an unavailable set (" + this + ')');
		System.out.println("Processing " + this);
		if (dryRun)
			return false;
		synchronized (this) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (parent) {
				try {
					Set<Path> roots = getRoots(invalidateCache);
					if (roots.size() > 1)
						throw new MameException("Multiple roots detected for (" + this + ") in " + roots);
					Path root = roots.iterator().next();
					Path parentDir = root.getParent().resolve(cloneof);
					Files.createDirectories(parentDir);
					Stream<Path> cloneFiles = Files.list(root);
					AtomicInteger errors = new AtomicInteger();
					AtomicInteger successes = new AtomicInteger();
					cloneFiles.forEach(srcFile -> {
						try {
							Path destFile = parentDir.resolve(srcFile.getFileName());
							Files.deleteIfExists(destFile);
							Files.move(srcFile, destFile);
							successes.getAndIncrement();
						} catch (IOException e) {
							errors.getAndIncrement();
							System.err.println("IO error merging '" + name + '\'');
							e.printStackTrace(System.err);
						}
					});
					if (errors.get() == 0 && Files.list(root).findAny().isEmpty())
						System.err.println("Unable to delete " + root);
					return successes.get() > 0;
				} catch (IOException e) {
					System.err.println("IO error preparing merging '" + name + '\'');
					e.printStackTrace(System.err);
					return false;
				}
			}
		}
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
