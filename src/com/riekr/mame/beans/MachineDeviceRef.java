package com.riekr.mame.beans;

import com.riekr.mame.tools.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineDeviceRef extends MameXmlChildOf<Machine> implements Serializable {

	@XmlAttribute
	public String name;

}
