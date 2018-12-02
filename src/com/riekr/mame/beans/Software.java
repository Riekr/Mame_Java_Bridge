package com.riekr.mame.beans;

import com.riekr.mame.attrs.AvailabilityCapable;
import com.riekr.mame.attrs.Completable;
import com.riekr.mame.attrs.ContainersCapable;
import com.riekr.mame.attrs.Mergeable;
import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Software extends ContainersCapable<SoftwareList> implements Serializable, Mergeable, Completable, AvailabilityCapable {

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

	private volatile Software _parent;

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

	@Nullable
	public Software getParent() {
		if (!isClone())
			return null;
		Sync.dcInit(this, () -> _parent == null, () -> {
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
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache) {
		Iterator<Path> i = getParentNode().availableContainers(complete, invalidateCache)
				.map(slRoot -> slRoot.resolve(name))
				.filter(candidate -> Files.isDirectory(candidate))
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
					Set<Path> roots = availableContainers(true, invalidateCache).collect(Collectors.toSet());
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
