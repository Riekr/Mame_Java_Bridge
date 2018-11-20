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
import java.util.concurrent.atomic.AtomicInteger;

@CommandLine.Command(name = "merge", description = "Convert software lists to merged sets (as they should be)")
public class SL_Merge implements Callable<Collection<Software>> {

	@CommandLine.Option(names = {"-t", "--dry-run"}, descriptionKey = "dryrun")
	public boolean dryRun = false;

	@CommandLine.Mixin
	public @NotNull SoftwareOptions softwareOptions = new SoftwareOptions();

	@Override
	public Collection<Software> call() {
		LinkedList<Software> res = new LinkedList<>();
		AtomicInteger checks = new AtomicInteger(0);
		softwareOptions.filterSoftwareStream(
				softwareOptions.filterSoftwareListStream(Mame.getInstance().softwareLists())
						.filter(SoftwareList::isAvailable)
						.flatMap(SoftwareList::softwares), false)
				.filter(s -> {
					checks.incrementAndGet();
					return s.isClone() && s.isAvailable();
				})
				.forEach(s -> {
					if (s.mergeIntoParent(dryRun))
						res.add(s);
				});
		System.out.println("Checked " + checks.get() + " software clones.");
		System.out.println("Merged " + res.size() + " softwares.");
		return res;
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_Merge(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
