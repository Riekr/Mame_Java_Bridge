package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MachineRom extends MachineComponent implements Serializable {

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

	private transient volatile Set<Path> _availableContainers;

	@NotNull
	public Stream<Path> availableContainers() {
		return availableContainers(false);
	}

	@NotNull
	public Stream<Path> availableContainers(boolean invalidateCache) {
		Sync.condInit(this, () -> _availableContainers == null || invalidateCache, () -> {
			Machine machine = getParentNode();
			Mame mame = machine.getParentNode().getParentNode();
			_availableContainers = new HashSet<>();
			do {
				for (Path romPath : mame.getRomPath()) {
					Path machineZipFile = romPath.resolve(machine.name + ".zip");
					if (Files.exists(machineZipFile)) {
						try (FileSystem machineZip = FileSystems.newFileSystem(machineZipFile, null)) {
							if (Files.exists(machineZip.getPath(name)))
								_availableContainers.add(machineZipFile);
						} catch (Exception e) {
							System.err.println("Unable to open " + machineZipFile);
							e.printStackTrace(System.err);
						}
					}
					Path machineDir = romPath.resolve(machine.name);
					if (Files.isDirectory(machineDir)) {
						Path rom = machineDir.resolve(name);
						if (Files.exists(rom))
							_availableContainers.add(machineDir);
					}
				}
				machine = machine.getParentMachine();
			} while (machine != null);
		});
		return _availableContainers.stream();
	}

	@Override
	public String toString() {
		return "[ROM:" + name + ']';
	}

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.ROM;
	}
}
