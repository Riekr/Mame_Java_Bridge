package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

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
	public List<MachineRom> roms;

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

	public boolean isBios() {
		return _isbios.val;
	}

	@Override
	public void setParentNode(@NotNull Machines parentNode) {
		super.setParentNode(parentNode);
		if (roms != null) {
			for (MachineRom r : roms)
				r.setParentNode(this);
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
