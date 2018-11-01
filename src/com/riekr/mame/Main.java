package com.riekr.mame;

import com.riekr.mame.callables.SL_BadDisks;
import com.riekr.mame.callables.SL_Incomplete;
import com.riekr.mame.callables.SL_Merge;
import com.riekr.mame.callables.SL_Missing;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;


@CommandLine.Command(subcommands = {
		Main.SL.class
})
public class Main {

	@CommandLine.Command(name = "sl", aliases = "software-lists", subcommands = {
			SL_BadDisks.class, SL_Incomplete.class, SL_Merge.class, SL_Missing.class
	})
	public static class SL {
		public static void main(String... args) {
			doMain(new SL(), args);
		}
	}

	public static void main(String... args) {
		doMain(new Main(), args);
	}

	private static void doMain(Object instance, String... args) {
		try {
			List<CommandLine> issuedCommands = new CommandLine(instance).parse(args);
			CommandLine cl = issuedCommands.get(issuedCommands.size() - 1);
			Object command = cl.getCommand();
			if (command instanceof Runnable)
				((Runnable) command).run();
			else if (command instanceof Callable<?>)
				((Callable) command).call();
			else
				cl.usage(System.out);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
