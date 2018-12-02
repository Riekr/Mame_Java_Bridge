package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class Machines extends MameXmlChildOf<Mame> implements Serializable {

	@XmlAttribute
	public int mameconfig; // TODO check mameconfig version change

	@XmlElement(name = "machine")
	private List<Machine> _machines;

	@Override
	public void setParentNode(@NotNull Mame parentNode) {
		super.setParentNode(parentNode);
		if (_machines != null) {
			for (Machine m : _machines)
				m.setParentNode(this);
		}
	}

	public int count() {
		return _machines == null ? 0 : _machines.size();
	}

	@NotNull
	public Stream<Machine> all() {
		return _machines == null ? Stream.empty() : _machines.stream();
	}
}
