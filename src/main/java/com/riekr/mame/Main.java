package com.riekr.mame;

import com.riekr.mame.callables.*;
import com.riekr.mame.utils.CLIUtils;
import picocli.CommandLine;

import static com.riekr.mame.utils.CLIUtils.doMain;


@CommandLine.Command(subcommands = {
		Main.M.class, Main.SL.class
})
public class Main {

	public static void main(String... args) {
		doMain(new Main(), args);
	}

	@CommandLine.Command(name = "m", aliases = "machines", description = "Machines operations", subcommands = {
			M_List.class, M_Containers.class, M_Clones.class, M_Missing.class, M_Available.class
	})
	static class M extends CLIUtils.UsageHelp {
		public static void main(String... args) {
			doMain(new SL(), args);
		}
	}

	@CommandLine.Command(name = "sl", aliases = "software-lists", description = "Software lists operations", subcommands = {
			SL_BadDisks.class, SL_Incomplete.class, SL_List.class, SL_Merge.class, SL_Missing.class
	})
	static class SL extends CLIUtils.UsageHelp {
		public static void main(String... args) {
			doMain(new SL(), args);
		}
	}
}
