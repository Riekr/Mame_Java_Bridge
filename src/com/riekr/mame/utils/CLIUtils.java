package com.riekr.mame.utils;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

public final class CLIUtils {

	private CLIUtils() {
	}

	public static class UsageHelp implements Runnable {

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
			cl.addMixin("MameConfig", Mame.DEFAULT_CONFIG_FACTORY);
			cl.setResourceBundle(ResourceBundle.getBundle("com.riekr.mame.callables.cmdline"));
			try {
				cl.parseWithHandler(new CommandLine.RunLast(), args);
			} catch (CommandLine.ExecutionException e) {
				throw e.getCause();
			}
		} catch (MameException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(2);
		}
	}

	@Nullable
	public static Path findExecInPath(@NotNull String... names) {
		if (names.length == 0)
			return null;
		List<String> searchPaths = new ArrayList<>();
		searchPaths.add(".");
		for (Map.Entry<String, String> e : System.getenv().entrySet()) {
			if (e.getKey().equalsIgnoreCase("PATH"))
				Collections.addAll(searchPaths, e.getValue().split("\\Q" + File.pathSeparatorChar + "\\E"));
		}
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			searchPaths.add("D:\\Giochi\\Mame");
		for (String s : searchPaths) {
			Path searchPath = Path.of(s);
			if (!Files.isDirectory(searchPath))
				continue;
			for (String name : names) {
				Path execFile = searchPath.resolve(name);
				if (Files.isExecutable(execFile))
					return execFile;
			}
		}
		return null;
	}
}
