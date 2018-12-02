package com.riekr.mame.beans;

import com.riekr.mame.attrs.RootList;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.tools.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@XmlRootElement(name = "softwarelists")
public class SoftwareLists extends MameXmlChildOf<Mame> implements Serializable, RootList<SoftwareList> {

	@XmlElement(name = "softwarelist")
	private List<SoftwareList> _lists;

	// TODO add mameconfig version and check it
	// TODO check SoftwareLists schema

	@Override
	public int count() {
		return _lists == null ? 0 : _lists.size();
	}

	@Override
	@NotNull
	public Stream<SoftwareList> all() {
		return _lists == null ? Stream.empty() : _lists.stream();
	}
}
