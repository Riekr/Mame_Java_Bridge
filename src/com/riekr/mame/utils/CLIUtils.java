package com.riekr.mame.utils;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public final class CLIUtils {

	private static class UsageHelp implements Runnable {

		@SuppressWarnings("unused")
		@CommandLine.Option(usageHelp = true, names = {"-h", "--help"}, descriptionKey = "help")
		private boolean _help;

		@Override
		public void run() {
			cl.usage(System.out);
		}
	}

	private static CommandLine cl;

	public static void doMain(@NotNull Object instance, String... args) {
		try {
			if (instance instanceof Runnable || instance instanceof Callable) {
				cl = new CommandLine(instance);
				cl.addMixin("CommandLineBase", new UsageHelp());
			} else {
				cl = new CommandLine(new UsageHelp());
				cl.addMixin(instance.getClass().getSimpleName(), instance);
			}
			cl.setResourceBundle(ResourceBundle.getBundle("com.riekr.mame.callables.cmdline"));
			cl.parseWithHandler(new CommandLine.RunLast(), args);
		} catch (CommandLine.ExecutionException e) {
			e.getCause().printStackTrace(System.err);
			System.exit(2);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
