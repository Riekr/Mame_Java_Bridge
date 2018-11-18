package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import com.riekr.mame.utils.PrintStreamTee;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@CommandLine.Command(name = "missing", description = "Lists all completely missing machines")
public class M_Missing implements Runnable {

	@CommandLine.Parameters(arity = "0..")
	public Set<String> names;

	@CommandLine.Option(names = "--output", description = "Write output to file <out> in addition to stdout")
	public File out;

	@CommandLine.Option(names = {"--mechanical", "-m"}, description = "Check only mechanical machines")
	public boolean mechanical;

	@CommandLine.Option(names = {"--device", "-d"}, description = "Check only device machines")
	public boolean device;

	@CommandLine.Option(names = {"--bios", "-b"}, description = "Check only bioses")
	public boolean bios;

	@CommandLine.Option(names = "--parallel", description = "Enable multi thread parallel processing, output will not be sorted")
	public boolean parallel;

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			s = s.filter(m -> names.contains(m.name));
		if (mechanical)
			s = s.filter(Machine::isMechanical);
		if (device)
			s = s.filter(Machine::isDevice);
		if (bios)
			s = s.filter(Machine::isBios);
		if (parallel)
			s = s.parallel();
		s = s.filter(m -> m.getAvailableContainers().isEmpty());
		AtomicInteger count = new AtomicInteger();
		PrintStream ps = PrintStreamTee.to(out);
		s.forEach(m -> {
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
