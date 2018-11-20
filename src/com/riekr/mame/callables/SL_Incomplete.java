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
import java.util.stream.Stream;

@CommandLine.Command(name = "incompletes", description = "Lists all available but incomplete software list entries")
public class SL_Incomplete implements Callable<Collection<Software>> {

	@CommandLine.Mixin
	public @NotNull SoftwareOptions softwareOptions = new SoftwareOptions();

	@Override
	public Collection<Software> call() {
		Collection<Software> res = new LinkedList<>();
		// software lists
		Stream<SoftwareList> softwareListStream = softwareOptions.filterSoftwareListStream(Mame.getInstance().softwareLists())
				.filter(SoftwareList::isAvailable);
		softwareOptions.filterSoftwareStreamOrAvailable(softwareListStream.flatMap(SoftwareList::softwares))
				.filter(s -> !s.isComplete())
				.forEach(s -> {
					System.out.println(s);
					res.add(s);
				});
		System.out.println("Found " + res.size() + " incomplete softwares.");
		return res;
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_Incomplete(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

}
