package com.riekr.mame.callables;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.mixins.SoftwareFilters;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.utils.CLIUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.function.Supplier;
import java.util.stream.Stream;

@CommandLine.Command(name = "list", description = "Lists all software list entries")
public class SL_List extends BaseSupplier<Stream<Software>> implements Runnable {

	@CommandLine.Mixin
	public @NotNull SoftwareFilters softwareFilters = new SoftwareFilters();

	public SL_List(@NotNull Supplier<Mame> mame) {
		super(mame);
	}

	@Override
	public Stream<Software> get() {
		return _mame.get().softwareLists()
				.filter(softwareFilters::softwareList)
				.flatMap(SoftwareList::softwares)
				.filter(softwareFilters::software);
	}

	@Override
	public void run() {
		get().forEach(System.out::println);
	}

	public static void main(String... args) {
		try {
			CLIUtils.doMain(new SL_List(Mame::getInstance), args);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
