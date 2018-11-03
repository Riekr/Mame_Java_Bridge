package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineRom extends MameXmlChildOf<Machine> implements Serializable {

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String bios;

	@XmlAttribute
	public long size;

	@XmlAttribute
	public String crc;

	@XmlAttribute
	public String sha1;

	@XmlAttribute
	public String merge;

	@XmlAttribute
	public String region;

	@XmlAttribute
	public String offset;

	@XmlAttribute
	public enDumpStatus status = enDumpStatus.good;

	@XmlAttribute
	public enYesNo optional = enYesNo.no;

}
