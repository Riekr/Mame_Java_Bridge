package com.riekr.mame.beans;

import com.riekr.mame.attrs.MachineComponent;

import java.util.stream.Stream;

public enum enMachineComponentType {
	ROM {
		@Override
		public Stream<? extends MachineComponent> streamFrom(Machine machine) {
			return machine.roms();
		}
	}, SAMPLE {
		@Override
		public Stream<? extends MachineComponent> streamFrom(Machine machine) {
			return machine.samples();
		}
	}, DISK {
		@Override
		public Stream<? extends MachineComponent> streamFrom(Machine machine) {
			return machine.disks();
		}
	};

	public abstract Stream<? extends MachineComponent> streamFrom(Machine machine);
}
