package com.riekr.mame.beans;

import com.riekr.mame.tools.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineBiosSet extends MameXmlChildOf<Machine> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String description;

	@XmlAttribute(name = "default")
	public enYesNo deflt = enYesNo.no;

}
