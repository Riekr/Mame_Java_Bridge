package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareDisk;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.ParallelOptions;
import com.riekr.mame.mixins.SoftwareFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@CommandLine.Command(name = "bad-disks", description = "Lists all software list entries with bad disks (invalid checksum)")
public class SL_BadDisks extends BaseSupplier<Stream<SoftwareDisk>> implements Runnable {

	@CommandLine.Mixin
	public @NotNull ParallelOptions parallelOptions = new ParallelOptions();

	@CommandLine.Mixin
	public @NotNull SoftwareFilters softwareFilters = new SoftwareFilters();

	public SL_BadDisks(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	@Override
	public Stream<SoftwareDisk> get() {
		return parallelOptions.parallelize(_mame.get().softwareLists())
				.filter(softwareFilters::softwareList)
				.filter(SoftwareList::isAvailable)
				.flatMap(SoftwareList::softwares)
				.filter(softwareFilters::software)
				.filter(Software::isAvailable)
				.flatMap(Software::disks)
				.filter(SoftwareDisk::isNotValid);
	}

	@Override
	public void run() {
		AtomicInteger count = new AtomicInteger();
		get().collect(groupingBy(SoftwareDisk::getSoftware))
				.forEach((software, softwareDisks) -> {
					System.out.println(software);
					softwareDisks.forEach(d -> {
						System.out.println("\t\"" + d.name + '"');
						count.incrementAndGet();
					});
				});
		System.out.println("Found " + count + " mismatched disks.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_BadDisks(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
