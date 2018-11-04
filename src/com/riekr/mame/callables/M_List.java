package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import picocli.CommandLine;

import java.util.stream.Stream;

@CommandLine.Command(name = "list", description = "Lists all mame machines")
public class M_List implements Runnable {

	@CommandLine.Option(names = {"-b", "--bios"})
	public boolean biosOnly;

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (biosOnly)
			s = s.filter(Machine::isBios);
		s.forEach(System.out::println);
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
