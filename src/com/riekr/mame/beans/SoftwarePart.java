package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class SoftwarePart extends MameXmlChildOf<Software> implements Serializable {

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
	public void setParentNode(@NotNull Software parentNode) {
		super.setParentNode(parentNode);
		if (_diskareas != null) {
			for (SoftwareDiskArea da : _diskareas)
				da.setParentNode(this);
		}
	}
}
