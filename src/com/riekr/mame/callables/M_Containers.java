package com.riekr.mame.callables;

import com.riekr.mame.attrs.ContainersCapable;
import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.MachineComponent;
import com.riekr.mame.beans.enMachineComponentType;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@CommandLine.Command(name = "containers", description = "Lists all found containers for this machine")
public class M_Containers implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@CommandLine.Option(names = "--component-type", description = "Limit missing file search to component type")
	public @Nullable enMachineComponentType componentType;

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			s = s.filter(m -> names.contains(m.name));
		s = machinesOptions.filter(s);
		s.forEach(m -> {
			System.out.println(m);
			Stream<? extends MachineComponent> c;
			if (componentType == null)
				c = m.components();
			else
				c = componentType.streamFrom(m);
			c.flatMap(ContainersCapable::unfold)
					.collect(groupingBy(cont -> cont.path))
					.forEach((path, containers)
							-> System.out.println("\t" + path + "\t" + containers.stream().collect(groupingBy(container -> container.comp.type())).keySet()));
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
