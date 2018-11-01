package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class SoftwareInfo extends MameXmlChildOf<Software> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String value;

}
