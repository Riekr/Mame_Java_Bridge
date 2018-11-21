package com.riekr.mame.beans;

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public class MachineSample extends MachineComponent implements Serializable {

	@XmlAttribute
	public String name;

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.SAMPLE;
	}

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl() {
		// TODO to be implemented
		return Collections.emptySet();
	}
}
