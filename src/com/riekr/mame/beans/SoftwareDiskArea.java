package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class SoftwareDiskArea extends MameXmlChildOf<SoftwarePart> implements Serializable {

	@XmlElement(name = "disk")
	private List<SoftwareDisk> _disks;

	public Stream<SoftwareDisk> disks() {
		return _disks == null ? Stream.empty() : _disks.stream();
	}

	@Override
	public void setParentNode(@NotNull SoftwarePart parentNode) {
		super.setParentNode(parentNode);
		if (_disks != null) {
			for (SoftwareDisk d : _disks)
				d.setParentNode(this);
		}
	}
}
