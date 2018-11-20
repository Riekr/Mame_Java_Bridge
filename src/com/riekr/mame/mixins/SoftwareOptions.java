package com.riekr.mame.mixins;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Set;
import java.util.stream.Stream;

public class SoftwareOptions {

	@CommandLine.Parameters(index = "0", descriptionKey = "softwareList", arity = "0..1")
	public String softwareList;

	@CommandLine.Parameters(index = "1..*", descriptionKey = "softwares")
	public Set<String> softwares;

	@NotNull
	public Stream<SoftwareList> filterSoftwareListStream(@NotNull Stream<SoftwareList> softwareListStream) {
		if (softwareList != null && !softwareList.isEmpty())
			softwareListStream = softwareListStream.filter(sl -> sl.name.equals(softwareList));
		return softwareListStream;
	}

	public Stream<Software> filterSoftwareStream(Stream<Software> softwareStream, boolean orAvailable) {
		if (softwares != null && !softwares.isEmpty())
			softwareStream = softwareStream.filter(s -> softwares.contains(s.name));
		else if (orAvailable)
			softwareStream = softwareStream.filter(Software::isAvailable);
		return softwareStream;
	}
}
