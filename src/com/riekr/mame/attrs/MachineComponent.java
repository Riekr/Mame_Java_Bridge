package com.riekr.mame.attrs;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.enMachineComponentType;

import java.io.Serializable;

public abstract class MachineComponent extends ContainersCapable<Machine> implements Serializable {

	public abstract enMachineComponentType type();

	public final Machine getMachine() {
		return getParentNode();
	}

}
