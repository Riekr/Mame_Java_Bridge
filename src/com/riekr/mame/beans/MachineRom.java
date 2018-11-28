package com.riekr.mame.beans;

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

public class MachineRom extends MachineComponent implements Serializable, Validable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String bios;

	@XmlAttribute
	public long size;

	@XmlAttribute
	public String crc;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String merge;

	@XmlAttribute
	public String region;

	@XmlAttribute
	public String offset;

	@XmlAttribute
	public enDumpStatus status = enDumpStatus.good;

	@XmlAttribute
	public enYesNo optional = enYesNo.no;

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean invalidateCache) {
		Machine machine = getParentNode();
		Mame mame = getMame();
		Set<Path> res = null;
		do {
			for (Path romPath : mame.getRomPath()) {
				Path machineZipFile = romPath.resolve(machine.name + ".zip");
				if (FSUtils.contains(machineZipFile, name, invalidateCache)) {
					if (STOP_ON_FIRST_AVAILABLE)
						return Collections.singleton(machineZipFile);
					(res == null ? res = new HashSet<>() : res).add(machineZipFile);
				}
				Path machineDir = romPath.resolve(machine.name);
				if (FSUtils.contains(machineDir, name, invalidateCache)) {
					if (STOP_ON_FIRST_AVAILABLE)
						return Collections.singleton(machineDir);
					(res == null ? res = new HashSet<>() : res).add(machineDir);
				}
			}
			machine = machine.getParentMachine();
		} while (machine != null);
		return res == null ? Collections.emptySet() : res;
	}

	@Override
	public boolean knownDumpExists() {
		return status == enDumpStatus.good;
	}

	@Override
	public String toString() {
		return "[ROM:" + name + ']';
	}

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.ROM;
	}

	@Override
	public boolean isValid(boolean invalidateCache) {
		return validateSha1(this, invalidateCache, sha1);
	}
}
