package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.mixins.MachinesFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@CommandLine.Command(name = "clones", description = "Lists all known clones for this machine")
public class M_Clones extends BaseSupplier<Stream<Machine>> implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@CommandLine.Mixin
	public @NotNull MachinesFilters machinesFilters = new MachinesFilters();

	public M_Clones(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	private boolean machineName(Machine m) {
		return names == null || names.isEmpty() || names.contains(m.name);
	}

	@Override
	public Stream<Machine> get() {
		return _mame.get().machines()
				.filter(this::machineName)
				.filter(machinesFilters)
				.flatMap(Machine::allClones);
	}

	@Override
	public void run() {
		get().collect(groupingBy(Machine::getParentMachine))
				.forEach((parent, clones) -> {
					System.out.println(parent);
					clones.forEach(clone -> System.out.println("\t" + clone));
				});
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Clones(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
