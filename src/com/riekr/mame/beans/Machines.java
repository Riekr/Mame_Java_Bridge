package com.riekr.mame.beans;

import com.riekr.mame.attrs.RootList;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.tools.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public class Machines extends MameXmlChildOf<Mame> implements Serializable, RootList<Machine> {

	@XmlAttribute
	public int mameconfig; // TODO check mameconfig version change

	@XmlElement(name = "machine")
	private List<Machine> _machines;

	@Override
	public int count() {
		return _machines == null ? 0 : _machines.size();
	}

	@Override
	@NotNull
	public Stream<Machine> all() {
		return _machines == null ? Stream.empty() : _machines.stream();
	}
}
