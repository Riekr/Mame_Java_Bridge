package com.riekr.mame.mixins;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import picocli.CommandLine;

import java.util.Set;
import java.util.stream.Stream;

public class SoftwareOptions {

	@CommandLine.Parameters(index = "0", descriptionKey = "softwareList", arity = "0..1")
	public String softwareList;

	@CommandLine.Parameters(index = "1..*", descriptionKey = "softwares")
	public Set<String> softwares;

	public Stream<SoftwareList> filterSoftwareListStream(Stream<SoftwareList> softwareListStream) {
		if (softwareList != null && !softwareList.isEmpty())
			softwareListStream = softwareListStream.filter(sl -> sl.name.equals(softwareList));
		return softwareListStream;
	}

	public Stream<Software> filterSoftwareStream(Stream<Software> softwareStream) {
		if (softwares != null && !softwares.isEmpty())
			softwareStream = softwareStream.filter(s -> softwares.contains(s.name));
		return softwareStream;
	}

	public Stream<Software> filterSoftwareStreamOrAvailable(Stream<Software> softwareStream) {
		if (softwares != null && !softwares.isEmpty())
			softwareStream = softwareStream.filter(s -> softwares.contains(s.name));
		else
			softwareStream = softwareStream.filter(Software::isAvailable);
		return softwareStream;
	}
}
