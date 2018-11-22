package com.riekr.mame.beans;

import com.riekr.mame.attrs.Validable;
import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
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
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean invalidateCache) {
		Machine machine = getParentNode();
		Mame mame = getMame();
		Set<Path> res = new HashSet<>();
		do {
			for (Path romPath : mame.getRomPath()) {
				Path machineDir = romPath.resolve(machine.name);
				if (Files.isDirectory(machineDir)) {
					Path rom = machineDir.resolve(name);
					if (Files.exists(rom))
						res.add(machineDir);
				}
			}
			// TODO can chds be merged? if not so remove parent loop
			machine = machine.getParentMachine();
		} while (machine != null);
		return res;
	}

	@Override
	public boolean isValid(boolean invalidateCache) {
		return validateSha1(this, invalidateCache, sha1);
	}
}
