package com.riekr.mame.beans;

import com.riekr.mame.tools.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;

public class MachineSoftwareList extends MameXmlChildOf<Machine> {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public enSoftwareListStatus status;

	@XmlAttribute
	public String filter;
}
