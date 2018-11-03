package com.riekr.mame.beans;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class MachineSample implements Serializable {

	@XmlAttribute
	public String name;

}
