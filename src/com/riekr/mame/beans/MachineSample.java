package com.riekr.mame.beans;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineSample extends MachineComponent implements Serializable {

	@XmlAttribute
	public String name;

	@Override
	public enMachineComponentType type() {
		return enMachineComponentType.SAMPLE;
	}
}
