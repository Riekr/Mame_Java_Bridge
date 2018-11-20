package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.SoftwareOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "missing", description = "Lists all unavailable software lists entries")
public class SL_Missing implements Callable<Collection<Software>> {

	@CommandLine.Mixin
	public @NotNull SoftwareOptions softwareOptions = new SoftwareOptions();

	@Override
	public Collection<Software> call() {
		final LinkedList<Software> res = new LinkedList<>();
		softwareOptions.filterSoftwareStream(
				softwareOptions.filterSoftwareListStream(Mame.getInstance().softwareLists())
						.filter(SoftwareList::isAvailable)
						.flatMap(SoftwareList::softwares)
		).filter(s -> !s.isAvailable())
				.forEach(s -> {
					System.out.println(s);
					res.add(s);
				});
		System.out.println("Found " + res.size() + " missing softwares.");
		return res;
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_Missing(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
