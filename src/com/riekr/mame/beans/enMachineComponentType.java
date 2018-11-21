package com.riekr.mame.beans;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public enum enMachineComponentType {
	ROM {
		@Override
		public Stream containersFrom(Machine machine, boolean invalidateCache) {
			return machine.romFiles(invalidateCache);
		}
	}, SAMPLE {
		@Override
		public Stream containersFrom(Machine machine, boolean invalidateCache) {
			return machine.sampleFiles(invalidateCache);
		}
	}, DISK {
		@Override
		public Stream containersFrom(Machine machine, boolean invalidateCache) {
			return machine.diskFiles(invalidateCache);
		}
	};

	public Stream<Containers<? extends MachineComponent>> containersFrom(Machine machine) {
		return containersFrom(machine, false);
	}

	public abstract Stream<Containers<? extends MachineComponent>> containersFrom(Machine machine, boolean invalidateCache);
}
