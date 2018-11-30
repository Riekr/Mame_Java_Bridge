package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.FSUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MachineSample extends MachineComponent implements Serializable {

	@XmlAttribute
	public String name;

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.SAMPLE;
	}

	@Override
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean complete, boolean invalidateCache) {
		Machine machine = getParentNode();
		Mame mame = getMame();
		Set<Path> res = null;
		do {
			for (Path samplesPath : mame.getSamplePath()) {
				Path samplesZipFile = samplesPath.resolve(machine.name + ".zip");
				if (FSUtils.contains(samplesZipFile, name, invalidateCache)) {
					if (complete)
						(res == null ? res = new HashSet<>() : res).add(samplesZipFile);
					else
						return Collections.singleton(samplesZipFile);
				}
				Path samplesDir = samplesPath.resolve(machine.name);
				if (FSUtils.contains(samplesDir, name, invalidateCache)) {
					if (complete)
						(res == null ? res = new HashSet<>() : res).add(samplesDir);
					else
						return Collections.singleton(samplesDir);
				}
			}
			machine = machine.getParentMachine();
		} while (machine != null);
		return res == null ? Collections.emptySet() : res;
	}

	@Override
	public boolean knownDumpExists() {
		return true;
	}
}
