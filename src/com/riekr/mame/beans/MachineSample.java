package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
	protected @NotNull Set<Path> getAvailableContainersImpl(boolean invalidateCache) {
		Machine machine = getParentNode();
		Mame mame = getMame();
		Set<Path> res = new HashSet<>();
		do {
			for (Path samplesPath : mame.getSamplePath()) {
				Path samplesZipFile = samplesPath.resolve(machine.name + ".zip");
				if (Files.exists(samplesZipFile)) {
					try (FileSystem samplesZip = FileSystems.newFileSystem(samplesZipFile, null)) {
						if (Files.exists(samplesZip.getPath(name)))
							res.add(samplesZipFile);
					} catch (Exception e) {
						System.err.println("Unable to open " + samplesZipFile);
						e.printStackTrace(System.err);
					}
				}
				Path samplesDir = samplesPath.resolve(machine.name);
				if (Files.isDirectory(samplesDir)) {
					Path sample = samplesDir.resolve(name);
					if (Files.exists(sample))
						res.add(samplesDir);
				}
			}
			machine = machine.getParentMachine();
		} while (machine != null);
		return res;
	}

	@Override
	public boolean knownDumpExists() {
		return true;
	}
}
