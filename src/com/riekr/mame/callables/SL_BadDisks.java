package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareDisk;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import picocli.CommandLine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(name = "bad-disks", description = "Lists all software list entries with bad disks (invalid checksum)")
public class SL_BadDisks extends FilterableSoftwareList implements Callable<Collection<SoftwareDisk>> {

	@Override
	public Collection<SoftwareDisk> call() {
		Collection<SoftwareDisk> res = new LinkedList<>();
		Stream<SoftwareList> softwareListStream = filterSoftwareListStream(Mame.getInstance().softwareLists())
				.filter(SoftwareList::isAvailable);
		filterSoftwareStream(softwareListStream.flatMap(SoftwareList::softwares))
				.filter(Software::isAvailable)
				.flatMap(Software::disks)
				.forEach(d -> {
					boolean valid = d.isValid();
					System.out.println('"' + d.name + "\" " + d.sha1 + ' ' + valid);
					if (!valid)
						res.add(d);
				});
		System.out.println("Found " + res.size() + " mismatched disks.");
		return res;
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_BadDisks(), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
