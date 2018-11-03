package com.riekr.mame.callables;

import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.tools.Mame;
import picocli.CommandLine;

import java.util.stream.Stream;

@CommandLine.Command(name = "list", description = "Lists all software list entries")
public class SL_List extends FilterableSoftwareList implements Runnable {

	@Override
	public void run() {
		Stream<SoftwareList> softwareListStream = filterSoftwareListStream(Mame.getInstance().softwareLists());
		filterSoftwareStream(softwareListStream.flatMap(SoftwareList::softwares))
				.forEach(System.out::println);
	}

	public static void main(String... args) {
		try {
			CommandLine.run(new SL_List(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
