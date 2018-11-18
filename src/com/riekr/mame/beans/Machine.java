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

	@XmlAttribute(name = "isdevice")
	private enYesNo _isdevice = no;

	@XmlAttribute(name = "ismechanical")
	private enYesNo _ismechanical = no;

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
	private Set<Machine> _directClones;
	private transient Map<File, Set<MachineComponent>> _containers;

	public boolean isBios() {
		return _isbios.val;
	}

	public boolean isDevice() {
		return _isdevice.val;
	}

	public boolean isMechanical() {
		return _ismechanical.val;
	}

	@NotNull
	public Stream<MachineRom> roms() {
		return _roms == null ? Stream.empty() : _roms.stream();
	}

	@NotNull
	public Machine getRootMachine() {
		Machine parent = getParentMachine();
		if (parent == null)
			return this;
		return parent.getRootMachine();
	}

	@NotNull
	public Stream<Machine> directClones() {
		if (_directClones == null) {
			_directClones = new HashSet<>();
			getParentNode().machines()
					.filter(m -> name.equals(m.cloneof))
					.collect(Collectors.toCollection(() -> _directClones));
			getParentNode().getParentNode().requestCachesWrite();
		}
		return _directClones.stream();
	}

	@NotNull
	public Stream<Machine> allClones() {
		Stream<Machine> res = directClones();
		for (Machine m : _directClones)
			res = Stream.concat(res, m.allClones());
		return res;
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
	public Map<File, Set<MachineComponent>> getAvailableContainers() {
		return getAvailableContainers(false);
	}

	@NotNull
	public Map<File, Set<MachineComponent>> getAvailableContainers(boolean invalidateCache) {
		if (_containers == null || invalidateCache) {
			_containers = new HashMap<>();
			roms().forEach(rom -> rom.availableContainers(invalidateCache)
					.forEach(file -> _containers.computeIfAbsent(file, k -> new HashSet<>()).add(rom)));
			// TODO disks, samples, bios, etc
		}
		return _containers;
	}

	@NotNull
	public Map<String, Set<MachineRom>> getSplitRomSet() {
		Map<String, Set<MachineRom>> res = new LinkedHashMap<>();
		Machine machine = this;
		do {
			Set<MachineRom> components = res.computeIfAbsent(machine.name, val -> new HashSet<>());
			if (_roms != null)
				components.addAll(_roms);
			machine = machine.getParentMachine();
		} while (machine != null);
		return res;
	}

	@NotNull
	public Map<String, Set<MachineRom>> getMergedRomSet() {
		Machine parent = getParentMachine();
		if (parent != null)
			return parent.getMergedRomSet();
		Map<String, Set<MachineRom>> res = new LinkedHashMap<>();
		Set<MachineRom> rootComponents = new TreeSet<>(Comparator.comparing(o -> o.name));
		res.put(name, rootComponents);
		if (_roms != null)
			rootComponents.addAll(_roms);
		allClones().forEach(cloneMachine -> cloneMachine.roms().forEach(cloneRom -> {
			if (rootComponents.contains(cloneRom))
				res.computeIfAbsent(cloneMachine.name, val -> new HashSet<>()).add(cloneRom);
			else
				rootComponents.add(cloneRom);
		}));
		return res;
	}

	// TODO full merged

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

	public String getTypeDescr() {
		StringBuilder res = new StringBuilder();
		getTypeDescr(res);
		return res.toString();
	}

	public void getTypeDescr(@NotNull StringBuilder res) {
		boolean close = false;
		if (isBios()) {
			close = true;
			res.append('[');
			res.append("BIOS");
		}
		if (isDevice()) {
			if (!close) {
				close = true;
				res.append('[');
			} else
				res.append(',');
			res.append("DEVICE");
		}
		if (isMechanical()) {
			if (!close) {
				close = true;
				res.append('[');
			} else
				res.append(',');
			res.append("MECH");
		}
		if (close)
			res.append(']');
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(name).append(": ").append(description).append(' ');
		getTypeDescr(res);
		return res.toString();
	}
}
