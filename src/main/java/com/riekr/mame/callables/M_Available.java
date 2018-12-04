package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.mixins.MachinesFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

@CommandLine.Command(name = "available", description = "Lists all available mame machines")
public class M_Available extends BaseSupplier<Stream<Machine>> implements Runnable {

	@CommandLine.Mixin
	public @NotNull MachinesFilters machinesFilters = new MachinesFilters();

	public M_Available(Supplier<Mame> mameSupplier) {
		super(mameSupplier);
	}

	@Override
	public Stream<Machine> get() {
		return _mame.get().machines()
				.filter(machinesFilters)
				.filter(Machine::isAvailable);
	}

	@Override
	public void run() {
		AtomicInteger count = new AtomicInteger();
		get().forEach(m -> {
			System.out.println(m);
			count.getAndIncrement();
		});
		System.out.println("Listed " + count + " available machines.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Available(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
