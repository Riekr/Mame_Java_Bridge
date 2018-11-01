package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "softwarelists")
public class SoftwareLists extends MameXmlChildOf<Mame> implements Serializable {

	@XmlElement(name = "softwarelist")
	public List<SoftwareList> lists;

	@Override
	public void setParentNode(@NotNull Mame parentNode) {
		super.setParentNode(parentNode);
		if (lists != null) {
			for (SoftwareList sl : lists)
				sl.setParentNode(this);
		}
	}

	@Override
	public String toString() {
		return String.valueOf(lists);
	}
}
