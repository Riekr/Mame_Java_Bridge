package com.riekr.mame.callables;

import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.SoftwareOptions;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.stream.Stream;

@CommandLine.Command(name = "list", description = "Lists all software list entries")
public class SL_List implements Runnable {

	@CommandLine.Mixin
	public @NotNull SoftwareOptions softwareOptions = new SoftwareOptions();

	@Override
	public void run() {
		Stream<SoftwareList> softwareListStream = softwareOptions.filterSoftwareListStream(Mame.getInstance().softwareLists());
		softwareOptions.filterSoftwareStream(softwareListStream.flatMap(SoftwareList::softwares))
				.forEach(System.out::println);
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_List(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
