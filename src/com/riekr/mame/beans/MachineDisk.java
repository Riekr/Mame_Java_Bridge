package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineDisk extends MameXmlChildOf<Machine> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String merge;

	@XmlAttribute
	public String region;

	@XmlAttribute
	public short index;

	@XmlAttribute
	public enYesNo writable = enYesNo.no;

	@XmlAttribute
	public enDumpStatus status = enDumpStatus.good;

	@XmlAttribute
	public enYesNo optional = enYesNo.no;

}
