package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CommandLine.Command(name = "available", description = "Lists all available mame machines")
public class M_Available implements Runnable {

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		s = machinesOptions.filter(s)
				.filter(Machine::isAvailable);
		AtomicInteger count = new AtomicInteger();
		s.forEach(m -> {
			System.out.println(m);
			count.getAndIncrement();
		});
		System.out.println("Listed " + count + " available machines.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Available(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
