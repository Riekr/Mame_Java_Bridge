package com.riekr.mame.callables;

import com.riekr.mame.beans.Containers;
import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.enMachineComponentType;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.mixins.ParallelOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import com.riekr.mame.utils.PrintStreamTee;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CommandLine.Command(name = "missing", description = "Lists all completely missing machines")
public class M_Missing implements Runnable {

	@CommandLine.Parameters(arity = "0..")
	public Set<String> names;

	@CommandLine.Option(names = "--output", description = "Write output to file <out> in addition to stdout")
	public Path out;

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@CommandLine.Mixin
	public @NotNull ParallelOptions parallelOptions = new ParallelOptions();

	@CommandLine.Option(names = "--component-type", description = "Limit missing file search to component type")
	public @NotNull enMachineComponentType componentType = enMachineComponentType.ROM;

	@Override
	public void run() {
		Stream<Machine> machines = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			machines = machines.filter(m -> names.contains(m.name));
		machines = machinesOptions.filter(machines);
		machines = parallelOptions.parallelize(machines);
		machines = machines.filter(machine -> componentType.containersFrom(machine).anyMatch(Containers::isEmpty));
		AtomicInteger count = new AtomicInteger();
		PrintStream ps = PrintStreamTee.to(out);
		machines.forEach(m -> {
			count.incrementAndGet();
			ps.println(m);
		});
		System.out.println("Found " + count + " missing roms.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Missing(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
