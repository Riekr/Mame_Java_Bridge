package com.riekr.mame.beans;

import com.riekr.mame.attrs.MachineComponent;
import com.riekr.mame.attrs.Validable;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.FSUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MachineDisk extends MachineComponent implements Serializable, Validable {

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
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache) {
		Machine machine = getParentNode();
		Mame mame = getMame();
		Set<Path> res = null;
		do {
			for (Path romPath : mame.getRomPath()) {
				Path machineDir = romPath.resolve(machine.name);
				if (FSUtils.contains(machineDir, name, invalidateCache)) {
					if (complete)
						(res == null ? res = new HashSet<>() : res).add(machineDir);
					else
						return Collections.singleton(machineDir);
				}
			}
			// TODO can chds be merged? if not so remove parent loop
			machine = machine.getParentMachine();
		} while (machine != null);
		return res == null ? Collections.emptySet() : res;
	}

	@Override
	public boolean knownDumpExists() {
		return status == enDumpStatus.good;
	}

	@Override
	public boolean isValid(boolean invalidateCache) {
		return validateSha1(invalidateCache, sha1);
	}
}
