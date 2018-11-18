package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	private transient volatile Set<File> _availableContainers;

	@NotNull
	public Stream<File> availableContainers() {
		return availableContainers(false);
	}

	@NotNull
	public Stream<File> availableContainers(boolean invalidateCache) {
		Sync.condInit(this, () -> _availableContainers == null || invalidateCache, () -> {
			Machine machine = getParentNode();
			Mame mame = machine.getParentNode().getParentNode();
			_availableContainers = new HashSet<>();
			do {
				for (File romPath : mame.getRomPath()) {
					File machineZipFile = new File(romPath, machine.name + ".zip");
					if (machineZipFile.canRead()) {
						try (ZipFile machineZip = new ZipFile(machineZipFile)) {
							ZipEntry romZipEntry = machineZip.getEntry(name);
							if (romZipEntry != null)
								_availableContainers.add(machineZipFile);
						} catch (Exception e) {
							System.err.println("Unable to open " + machineZipFile);
							e.printStackTrace(System.err);
						}
					} else {
						File machineDir = new File(romPath, machine.name);
						if (machineDir.isDirectory()) {
							File rom = new File(machineDir, name);
							if (rom.isFile())
								_availableContainers.add(machineDir);
						}
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
