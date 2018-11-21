package com.riekr.mame.beans;

import java.io.Serializable;

public abstract class MachineComponent extends ContainersCapable<Machine> implements Serializable {

	public abstract enMachineComponentType type();

}
