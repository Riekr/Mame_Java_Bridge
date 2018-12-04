package com.riekr.mame.beans;

import com.riekr.mame.attrs.SoftwareComponent;
import com.riekr.mame.tools.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class SoftwareDiskArea extends MameXmlChildOf<SoftwarePart> implements Serializable, SoftwareComponent {

	@XmlElement(name = "disk")
	private List<SoftwareDisk> _disks;

	public Stream<SoftwareDisk> disks() {
		return _disks == null ? Stream.empty() : _disks.stream();
	}

	@Override
	@NotNull
	public Software getSoftware() {
		return getParentNode().getSoftware();
	}
}
