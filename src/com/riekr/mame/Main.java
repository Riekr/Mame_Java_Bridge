package com.riekr.mame;

import com.riekr.mame.callables.SL_BadDisks;
import com.riekr.mame.callables.SL_Incomplete;
import com.riekr.mame.callables.SL_Merge;
import com.riekr.mame.callables.SL_Missing;
import com.riekr.mame.utils.CLIUtils;
import picocli.CommandLine;

import static com.riekr.mame.utils.CLIUtils.doMain;


@CommandLine.Command(subcommands = {
		Main.SL.class
})
public class Main {

	public static void main(String... args) {
		doMain(new Main(), args);
	}

	@CommandLine.Command(name = "sl", aliases = "software-lists", description = "Software lists operations", subcommands = {
			SL_BadDisks.class, SL_Incomplete.class, SL_Merge.class, SL_Missing.class
	})
	static class SL extends CLIUtils.UsageHelp {
		public static void main(String... args) {
			doMain(new SL(), args);
		}
	}
}
