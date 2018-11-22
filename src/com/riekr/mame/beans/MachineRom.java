package com.riekr.mame.beans;

import com.riekr.mame.attrs.Validable;
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
		Set<Path> res = new HashSet<>();
		do {
			for (Path romPath : mame.getRomPath()) {
				Path machineZipFile = romPath.resolve(machine.name + ".zip");
				if (Files.exists(machineZipFile)) {
					try (FileSystem machineZip = FileSystems.newFileSystem(machineZipFile, null)) {
						if (Files.exists(machineZip.getPath(name)))
							res.add(machineZipFile);
					} catch (Exception e) {
						System.err.println("Unable to open " + machineZipFile);
						e.printStackTrace(System.err);
					}
				}
				Path machineDir = romPath.resolve(machine.name);
				if (Files.isDirectory(machineDir)) {
					Path rom = machineDir.resolve(name);
					if (Files.exists(rom))
						res.add(machineDir);
				}
			}
			machine = machine.getParentMachine();
		} while (machine != null);
		return res;
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
