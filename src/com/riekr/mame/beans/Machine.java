package com.riekr.mame.beans;

import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.riekr.mame.beans.enYesNo.no;
import static com.riekr.mame.beans.enYesNo.yes;

public class Machine extends MameXmlChildOf<Machines> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute(name = "isbios")
	private enYesNo _isbios = no;

	@XmlAttribute
	public enYesNo isdevice = no;

	@XmlAttribute
	public enYesNo ismechanical = no;

	@XmlAttribute
	public enYesNo runnable = yes;

	@XmlAttribute
	public String cloneof;

	@XmlAttribute
	public String romof;

	@XmlAttribute
	public String sampleof;

	@XmlElement
	public String description;

	@XmlElement
	public short year;

	@XmlElement
	public String manufacturer;

	@XmlElement(name = "rom")
	private List<MachineRom> _roms;

	@XmlElement(name = "device_ref")
	public List<MachineDeviceRef> deviceRefs;

	@XmlElement(name = "sample")
	public List<MachineSample> samples;

	@XmlElement(name = "biosset")
	public List<MachineBiosSet> biosSets;

	@XmlElement(name = "disk")
	public List<MachineDisk> disks;

	@XmlElement(name = "softwarelist")
	public List<MachineSoftwareList> softwareLists;

	private Machine _parentMachine;
	private transient Map<File, Set<MachineComponent>> _containers;

	public boolean isBios() {
		return _isbios.val;
	}

	@NotNull
	public Stream<MachineRom> roms() {
		return _roms == null ? Stream.empty() : _roms.stream();
	}

	@Nullable
	public Machine getParentMachine() {
		if (cloneof == null)
			return null;
		if (_parentMachine == null) {
			Machines machines = getParentNode();
			List<Machine> res = machines.machines()
					.filter(m -> cloneof.equals(m.name))
					.collect(Collectors.toList());
			switch (res.size()) {
				case 0:
					throw new MameException("No parent of " + name + " found");
				case 1:
					_parentMachine = res.get(0);
					machines.getParentNode().requestCachesWrite();
					break;
				default:
					throw new MameException("Found multiple parents of " + name + " " + res);
			}
		}
		return _parentMachine;
	}

	@NotNull
	public Map<File, Set<MachineComponent>> getContainers() {
		return getContainers(false);
	}

	@NotNull
	public Map<File, Set<MachineComponent>> getContainers(boolean invalidateCache) {
		if (_containers == null || invalidateCache) {
			_containers = new HashMap<>();
			roms().forEach(rom -> rom.containers(invalidateCache)
					.forEach(file -> _containers.computeIfAbsent(file, k -> new HashSet<>()).add(rom)));
			// TODO disks, samples, bios, etc
		}
		return _containers;
	}


	@Override
	public void setParentNode(@NotNull Machines parentNode) {
		super.setParentNode(parentNode);
		if (_roms != null) {
			for (MachineRom r : _roms)
				r.setParentNode(this);
		}
		if (samples != null) {
			for (MachineSample s : samples)
				s.setParentNode(this);
		}
		if (biosSets != null) {
			for (MachineBiosSet b : biosSets)
				b.setParentNode(this);
		}
		if (disks != null) {
			for (MachineDisk d : disks)
				d.setParentNode(this);
		}
	}

	@Override
	public String toString() {
		return name + ": " + description;
	}
}
