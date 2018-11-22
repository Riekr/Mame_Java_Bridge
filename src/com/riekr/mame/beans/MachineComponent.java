package com.riekr.mame.beans;

import com.riekr.mame.attrs.ContainersCapable;

import java.io.Serializable;

public abstract class MachineComponent extends ContainersCapable<Machine> implements Serializable {

	public abstract enMachineComponentType type();

}
