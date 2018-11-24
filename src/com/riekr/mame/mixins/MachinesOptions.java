package com.riekr.mame.mixins;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.enMachineHeritage;
import com.riekr.mame.beans.enYesNo;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.stream.Stream;

public class MachinesOptions {

	@CommandLine.Option(names = {"--mechanical"}, description = "Check only mechanical machines (${COMPLETION-CANDIDATES})")
	public enYesNo mechanical;

	@CommandLine.Option(names = {"--device"}, description = "Check only device machines (${COMPLETION-CANDIDATES})")
	public enYesNo device;

	@CommandLine.Option(names = {"--bios"}, description = "Check only bioses (${COMPLETION-CANDIDATES})")
	public enYesNo bios;

	@CommandLine.Option(names = {"--heritage"}, description = "Check only parent/clone machines (${COMPLETION-CANDIDATES})")
	public enMachineHeritage heritage;

	@NotNull
	public Stream<Machine> filter(@NotNull Stream<Machine> s) {
		if (mechanical != null)
			s = s.filter(m -> m.isMechanical() == mechanical.val);
		if (device != null)
			s = s.filter(m -> m.isDevice() == device.val);
		if (bios != null)
			s = s.filter(m -> m.isBios() == bios.val);
		if (heritage != null)
			s = s.filter(m -> m.getHeritage() == heritage);
		return s;
	}
}
