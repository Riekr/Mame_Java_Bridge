package com.riekr.mame.callables;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.mixins.MachinesOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Set;
import java.util.stream.Stream;

@CommandLine.Command(name = "clones", description = "Lists all known clones for this machine")
public class M_Clones implements Runnable {

	@CommandLine.Parameters(arity = "1..")
	public Set<String> names;

	@CommandLine.Mixin
	public @NotNull MachinesOptions machinesOptions = new MachinesOptions();

	@Override
	public void run() {
		Stream<Machine> s = Mame.getInstance().machines();
		if (names != null && !names.isEmpty())
			s = s.filter(m -> names.contains(m.name));
		s = machinesOptions.filter(s);
		s.forEach(m -> {
			System.out.println(m);
			m.allClones().forEach(clone -> System.out.println("\t" + clone));
		});
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new M_Clones(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
