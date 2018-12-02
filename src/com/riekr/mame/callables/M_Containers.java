package com.riekr.mame.callables;

import com.riekr.mame.attrs.ContainersCapable;
import com.riekr.mame.beans.Container;
import com.riekr.mame.beans.Machine;
import com.riekr.mame.attrs.MachineComponent;
import com.riekr.mame.beans.enMachineComponentType;
import com.riekr.mame.mixins.MachinesFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@CommandLine.Command(name = "containers", description = "Lists all found containers for this machine")
public class M_Containers extends BaseSupplier<Stream<Container<? extends MachineComponent>>> implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@CommandLine.Mixin
	public @NotNull MachinesFilters machinesFilters = new MachinesFilters();

	@CommandLine.Option(names = "--component-type", description = "Limit missing file search to component type")
	public @Nullable enMachineComponentType componentType;

	public M_Containers(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	private boolean machineName(Machine m) {
		return names == null || names.isEmpty() || names.contains(m.name);
	}

	@Override
	public Stream<Container<? extends MachineComponent>> get() {
		return _mame.get().machines()
				.filter(this::machineName)
				.filter(machinesFilters)
				.flatMap(machine -> {
					if (componentType == null)
						return machine.components();
					return componentType.streamFrom(machine);
				})
				.flatMap(ContainersCapable::unfold);
	}

	@Override
	public void run() {
		get().collect(groupingBy(container -> container.comp.getMachine()))
				.forEach((machine, containers) -> {
					System.out.println(machine);
					containers.stream()
							.collect(groupingBy(cont -> cont.path))
							.forEach((path, containersByPath)
									-> System.out.println(
									/**/"\t" + path.normalize() +
											"\t" + containersByPath.stream().collect(groupingBy(container -> container.comp.type())).keySet()));
				});
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Containers(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
