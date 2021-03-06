package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.attrs.MachineComponent;
import com.riekr.mame.beans.enMachineComponentType;
import com.riekr.mame.beans.enYesNo;
import com.riekr.mame.mixins.MachinesFilters;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

@CommandLine.Command(name = "missing", description = "Lists all completely missing machines")
public class M_Missing extends BaseSupplier<Stream<Machine>> implements Runnable {

	@CommandLine.Parameters(arity = "0..")
	public Set<String> names;

	@CommandLine.Option(names = "--output", description = "Write output to file <out> in addition to stdout")
	public Path out;

	@CommandLine.Mixin
	public @NotNull MachinesFilters machinesFilters = new MachinesFilters() {{
		device = enYesNo.no;
		bios = enYesNo.no;
		mechanical = enYesNo.no;
	}};

	@CommandLine.Option(names = "--runnable", description = "Filter by machine runnable status (${COMPLETION-CANDIDATES})")
	public enYesNo runnable;

	@CommandLine.Mixin
	public @NotNull ParallelOptions parallelOptions = new ParallelOptions();

	@CommandLine.Option(names = "--component-type", description = "Limit missing file search to component type")
	public @NotNull enMachineComponentType componentType = enMachineComponentType.ROM;

	public M_Missing(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	private boolean machineRunnable(Machine m) {
		return runnable == null || m.runnable == runnable;
	}

	private boolean missesAnyComponent(Machine m) {
		return componentType.streamFrom(m)
				.filter(MachineComponent::knownDumpExists)
				.anyMatch(MachineComponent::isNotAvailable);
	}

	@Override
	public Stream<Machine> get() {
		return parallelOptions.parallelize(_mame.get().machines(names))
				.filter(machinesFilters)
				.filter(this::machineRunnable)
				.filter(this::missesAnyComponent);
	}

	@Override
	public void run() {
		AtomicInteger count = new AtomicInteger();
		PrintStream ps = PrintStreamTee.to(out);
		get().forEach(m -> {
			count.incrementAndGet();
			ps.println(m);
		});
		System.out.println("Found " + count + " missing roms.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Missing(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
