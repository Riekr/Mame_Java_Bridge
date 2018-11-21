package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.MachineComponent;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "containers", description = "Lists all found containers for this machine")
public class M_Containers implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			s = s.filter(m -> names.contains(m.name));
		s = machinesOptions.filter(s);
		s.forEach(m -> {
			System.out.println(m);
			Map<Path, Set<MachineComponent>> containers = m.getAvailableContainers();
			for (Map.Entry<Path, Set<MachineComponent>> e : containers.entrySet()) {
				System.out.println("\t" + e.getKey() + "\t" + e.getValue().stream().collect(Collectors.groupingBy(MachineComponent::type)).keySet());
			}
		});
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Containers(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
