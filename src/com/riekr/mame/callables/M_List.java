package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CommandLine.Command(name = "list", description = "Lists all mame machines")
public class M_List implements Runnable {

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		s = machinesOptions.filter(s);
		AtomicInteger count = new AtomicInteger();
		s.forEach(m -> {
			System.out.println(m);
			count.getAndIncrement();
		});
		System.out.println("Listed " + count + " machines.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_List(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
