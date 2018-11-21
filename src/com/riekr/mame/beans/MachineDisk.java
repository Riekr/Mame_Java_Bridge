package com.riekr.mame.beans;

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class MachineDisk extends MachineComponent implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String merge;

	@XmlAttribute
	public String region;

	@XmlAttribute
	public short index;

	@XmlAttribute
	public enYesNo writable = enYesNo.no;

	@XmlAttribute
	public enDumpStatus status = enDumpStatus.good;

	@XmlAttribute
	public enYesNo optional = enYesNo.no;

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.DISK;
	}

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean invalidateCache) {
		// TODO to be implemented
		return Collections.emptySet();
	}
}
