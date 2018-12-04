package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.SoftwareFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;

@CommandLine.Command(name = "merge", description = "Convert software lists to merged sets (as they should be)")
public class SL_Merge implements Runnable {

	@CommandLine.Option(names = {"-t", "--dry-run"}, descriptionKey = "dryrun")
	public boolean dryRun = true;

	@CommandLine.Mixin
	public @NotNull SoftwareFilters softwareFilters = new SoftwareFilters();

	@Override
	public void run() {
		AtomicInteger checked = new AtomicInteger(0);
		AtomicInteger merged = new AtomicInteger(0);
		Mame.getInstance().softwareLists()
				.filter(softwareFilters::softwareList)
				.flatMap(SoftwareList::softwares)
				.filter(softwareFilters::software)
				.filter(Software::isClone)
				.filter(Software::isAvailable)
				.forEach(s -> {
					if (s.mergeIntoParent(dryRun))
						merged.incrementAndGet();
				});
		System.out.println("Checked " + checked + " software clones.");
		System.out.println("Merged " + merged + " softwares.");
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
