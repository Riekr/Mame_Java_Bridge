package com.riekr.mame.beans;

import com.riekr.mame.utils.MameXmlChildOf;

import java.io.Serializable;

public abstract class MachineComponent extends MameXmlChildOf<Machine> implements Serializable {

	public abstract enMachineComponentType type();

}
