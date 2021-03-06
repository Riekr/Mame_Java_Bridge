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

@CommandLine.Command(name = "list", description = "Lists all mame machines")
public class M_List extends BaseSupplier<Stream<Machine>> implements Runnable {

	@CommandLine.Mixin
	public @NotNull MachinesFilters machinesFilters = new MachinesFilters();

	public M_List(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	@Override
	public Stream<Machine> get() {
		return _mame.get().machines()
				.filter(machinesFilters);
	}

	@Override
	public void run() {
		AtomicInteger count = new AtomicInteger();
		get().forEach(m -> {
			System.out.println(m);
			count.getAndIncrement();
		});
		System.out.println("Listed " + count + " machines.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_List(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
