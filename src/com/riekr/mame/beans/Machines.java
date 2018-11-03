package com.riekr.mame.beans;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

public class Machines extends MameXmlChildOf<Mame> implements Serializable {

	@XmlAttribute
	public int mameconfig;

	@XmlElement(name = "machine")
	public List<Machine> machines;

	@Override
	public void setParentNode(@NotNull Mame parentNode) {
		super.setParentNode(parentNode);
		if (machines != null) {
			for (Machine m : machines)
				m.setParentNode(this);
		}
	}
}
