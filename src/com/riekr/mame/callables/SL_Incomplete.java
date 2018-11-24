package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.SoftwareFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

@CommandLine.Command(name = "incompletes", description = "Lists all available but incomplete software list entries")
public class SL_Incomplete extends BaseSupplier<Stream<Software>> implements Runnable {

	@CommandLine.Mixin
	public @NotNull SoftwareFilters softwareFilters = new SoftwareFilters();

	public SL_Incomplete(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	@Override
	public Stream<Software> get() {
		return _mame.get().softwareLists()
				.filter(softwareFilters::softwareList)
				.filter(SoftwareList::isAvailable)
				.flatMap(SoftwareList::softwares)
				.filter(softwareFilters::softwareOrAvailable)
				.filter(Software::isNotComplete);
	}

	@Override
	public void run() {
		AtomicInteger count = new AtomicInteger();
		get().forEach(s -> {
			System.out.println(s);
			count.incrementAndGet();
		});
		System.out.println("Found " + count + " incomplete softwares.");
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_Incomplete(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
