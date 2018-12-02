package com.riekr.mame.beans;

import com.riekr.mame.attrs.AvailabilityCapable;
import com.riekr.mame.attrs.Completable;
import com.riekr.mame.attrs.Mergeable;
import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.MameXmlChildOf;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.riekr.mame.beans.enYesNo.no;
import static com.riekr.mame.beans.enYesNo.yes;

public class Machine extends MameXmlChildOf<Machines> implements Serializable, Mergeable, Completable, AvailabilityCapable {

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
	private List<MachineSample> _samples;

	@XmlElement(name = "biosset")
	public List<MachineBiosSet> biosSets;

	@XmlElement(name = "disk")
	private List<MachineDisk> _disks;

	@XmlElement(name = "softwarelist")
	public List<MachineSoftwareList> softwareLists;

	private volatile Machine _parentMachine;
	private volatile Set<Machine> _directClones;
	private volatile Map<String, Set<MachineRom>> _splitRomSet;
	private volatile Map<String, Set<MachineRom>> _mergedRomSet;

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
	public Stream<MachineSample> samples() {
		return _samples == null ? Stream.empty() : _samples.stream();
	}

	@NotNull
	public Stream<MachineDisk> disks() {
		return _disks == null ? Stream.empty() : _disks.stream();
	}

	@NotNull
	public Stream<MachineComponent> components() {
		return Stream.concat(Stream.concat(roms(), samples()), disks());
	}

	@NotNull
	public Stream<Machine> directClones() {
		Sync.dcInit(this, () -> _directClones == null, () -> {
			_directClones = new HashSet<>();
			getParentNode().all()
					.filter(m -> name.equals(m.cloneof))
					.collect(Collectors.toCollection(() -> _directClones));
			notifyCachedDataChanged();
		});
		return _directClones.stream();
	}

	@NotNull
	public Stream<Machine> allClones() {
		Stream<Machine> res = directClones();
		for (Machine m : _directClones)
			res = Stream.concat(res, m.allClones());
		return res;
	}

	@NotNull
	public Machine getRootMachine() {
		Machine parent = getParentMachine();
		if (parent == null)
			return this;
		return parent.getRootMachine();
	}

	@Nullable
	public Machine getParentMachine() {
		if (cloneof == null)
			return null;
		Sync.dcInit(this, () -> _parentMachine == null, () -> {
			List<Machine> res = getParentNode().all()
					.filter(m -> cloneof.equals(m.name))
					.collect(Collectors.toList());
			switch (res.size()) {
				case 0:
					throw new MameException("Parent of " + name + " not found (" + cloneof + ')');
				case 1:
					_parentMachine = res.get(0);
					notifyCachedDataChanged();
					break;
				default:
					throw new MameException("Found multiple parents of " + name + " " + res);
			}
		});
		return _parentMachine;
	}

	@NotNull
	public Map<String, Set<MachineRom>> getSplitRomSet() {
		Sync.dcInit(this, () -> _splitRomSet == null, () -> {
			_splitRomSet = new LinkedHashMap<>();
			Machine machine = this;
			do {
				Set<MachineRom> components = _splitRomSet.computeIfAbsent(machine.name, val -> new HashSet<>());
				if (_roms != null)
					components.addAll(_roms);
				machine = machine.getParentMachine();
			} while (machine != null);
			notifyCachedDataChanged();
		});
		return _splitRomSet;
	}

	@NotNull
	public Map<String, Set<MachineRom>> getMergedRomSet() {
		Sync.dcInit(this, () -> _mergedRomSet == null, () -> {
			Machine parent = getParentMachine();
			if (parent != null) {
				_mergedRomSet = parent.getMergedRomSet();
				return;
			}
			_mergedRomSet = new LinkedHashMap<>();
			Set<MachineRom> rootComponents = new TreeSet<>(Comparator.comparing(o -> o.name));
			_mergedRomSet.put(name, rootComponents);
			if (_roms != null)
				rootComponents.addAll(_roms);
			allClones().forEach(cloneMachine -> cloneMachine.roms().forEach(cloneRom -> {
				if (rootComponents.contains(cloneRom))
					_mergedRomSet.computeIfAbsent(cloneMachine.name, val -> new HashSet<>()).add(cloneRom);
				else
					rootComponents.add(cloneRom);
			}));
			notifyCachedDataChanged();
		});
		return _mergedRomSet;
	}

	@Override
	public boolean mergeIntoParent(boolean dryRun, boolean invalidateCache) {
		// TODO to be implemented
		throw new UnsupportedOperationException();
	}

	// TODO full merged

	@Override
	public void setParentNode(@NotNull Machines parentNode) {
		super.setParentNode(parentNode);
		if (_roms != null) {
			for (MachineRom r : _roms)
				r.setParentNode(this);
		}
		if (_samples != null) {
			for (MachineSample s : _samples)
				s.setParentNode(this);
		}
		if (biosSets != null) {
			for (MachineBiosSet b : biosSets)
				b.setParentNode(this);
		}
		if (_disks != null) {
			for (MachineDisk d : _disks)
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

	@Override
	public boolean isComplete(boolean invalidateCache) {
		return components().allMatch(machineComponent -> machineComponent.isAvailable(invalidateCache));
	}

	@Override
	public boolean isAvailable(boolean invalidateCache) {
		return roms().anyMatch(rom -> rom.isAvailable(invalidateCache));
	}

	@NotNull
	public enMachineHeritage getHeritage() {
		return getParentMachine() == null ? enMachineHeritage.PARENT : enMachineHeritage.CLONE;
	}
}
