package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.MachineComponent;
import com.riekr.mame.tools.Mame;
import picocli.CommandLine;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "containers", description = "Lists all found containers for this machine")
public class M_Containers implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			s = s.filter(m -> names.contains(m.name));
		s.forEach(m -> {
			System.out.println(m);
			Map<File, Set<MachineComponent>> containers = m.getContainers();
			for (Map.Entry<File, Set<MachineComponent>> e : containers.entrySet()) {
				System.out.println("\t" + e.getKey() + "\t" + e.getValue().stream().collect(Collectors.groupingBy(MachineComponent::type)).keySet());
			}
		});
	}

	public static void main(String... args) {
		try {
			CommandLine.run(new M_Containers(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
