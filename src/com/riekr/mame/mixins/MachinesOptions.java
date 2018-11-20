package com.riekr.mame.mixins;

import com.riekr.mame.beans.Machine;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.stream.Stream;

public class MachinesOptions {
	@CommandLine.Option(names = {"--mechanical", "-m"}, description = "Check only mechanical machines")
	public boolean mechanical;

	@CommandLine.Option(names = {"--device", "-d"}, description = "Check only device machines")
	public boolean device;

	@CommandLine.Option(names = {"--bios", "-b"}, description = "Check only bioses")
	public boolean bios;

	@NotNull
	public Stream<Machine> filter(@NotNull Stream<Machine> s) {
		if (mechanical)
			s = s.filter(Machine::isMechanical);
		if (device)
			s = s.filter(Machine::isDevice);
		if (bios)
			s = s.filter(Machine::isBios);
		return s;
	}

}
