package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Set;

public abstract class MachineComponent extends MameXmlChildOf<Machine> implements Serializable {

	public abstract enMachineComponentType type();

	@NotNull
	public final Set<Path> availableContainers() {
		return availableContainers(false);
	}

	@NotNull
	public abstract Set<Path> availableContainers(boolean invalidateCache);

}
