package com.riekr.mame.mixins;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.enMachineHeritage;
import com.riekr.mame.beans.enYesNo;
import picocli.CommandLine;

import java.util.function.Predicate;

public class MachinesFilters implements Predicate<Machine> {

	@CommandLine.Option(names = {"--mechanical"}, description = "Check only mechanical machines (${COMPLETION-CANDIDATES})")
	public enYesNo mechanical;

	@CommandLine.Option(names = {"--device"}, description = "Check only device machines (${COMPLETION-CANDIDATES})")
	public enYesNo device;

	@CommandLine.Option(names = {"--bios"}, description = "Check only bioses (${COMPLETION-CANDIDATES})")
	public enYesNo bios;

	@CommandLine.Option(names = {"--heritage"}, description = "Check only parent/clone machines (${COMPLETION-CANDIDATES})")
	public enMachineHeritage heritage;

	@Override
	public boolean test(Machine m) {
		if (mechanical != null && m.isMechanical() != mechanical.val)
			return false;
		if (device != null && m.isDevice() != device.val)
			return false;
		if (bios != null && m.isBios() != bios.val)
			return false;
		return heritage == null || m.getHeritage() == heritage;
	}
}
