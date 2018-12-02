package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

@XmlRootElement(name = "softwarelists")
public class SoftwareLists extends MameXmlChildOf<Mame> implements Serializable {

	@XmlElement(name = "softwarelist")
	private List<SoftwareList> _lists;

	// TODO add mameconfig version and check it
	// TODO check SoftwareLists schema

	@Override
	public void setParentNode(@NotNull Mame parentNode) {
		super.setParentNode(parentNode);
		if (_lists != null) {
			for (SoftwareList sl : _lists)
				sl.setParentNode(this);
		}
	}

	public int count() {
		return _lists == null ? 0 : _lists.size();
	}

	@NotNull
	public Stream<SoftwareList> all() {
		return _lists == null ? Stream.empty() : _lists.stream();
	}
}
