package com.riekr.mame.beans;

import com.riekr.mame.attrs.SoftwareComponent;
import com.riekr.mame.tools.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class SoftwarePart extends MameXmlChildOf<Software> implements Serializable, SoftwareComponent {

	@XmlAttribute
	public String name;

	@XmlAttribute(name = "interface")
	public String intrface;

	@XmlElement(name = "diskarea")
	private List<SoftwareDiskArea> _diskareas;

	public Stream<SoftwareDiskArea> diskAreas() {
		return _diskareas == null ? Stream.empty() : _diskareas.stream();
	}

	public Stream<SoftwareDisk> disks() {
		return diskAreas().flatMap(SoftwareDiskArea::disks);
	}

	@Override
	@NotNull
	public Software getSoftware() {
		return getParentNode();
	}

}
