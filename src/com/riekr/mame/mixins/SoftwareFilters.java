package com.riekr.mame.mixins;

import com.riekr.mame.beans.Software;
import com.riekr.mame.beans.SoftwareList;
import picocli.CommandLine;

import java.util.Set;

public class SoftwareFilters {

	@CommandLine.Parameters(index = "0", descriptionKey = "softwareList", arity = "0..1")
	public String softwareList;

	@CommandLine.Parameters(index = "1..*", descriptionKey = "softwares")
	public Set<String> softwares;

	public boolean softwareList(SoftwareList sl) {
		return softwareList == null || softwareList.isEmpty() || sl.name.equals(softwareList);
	}

	public boolean software(Software s) {
		return softwares == null || softwares.isEmpty() || softwares.contains(s.name);
	}

	public boolean softwareOrAvailable(Software s) {
		if (softwares == null || softwares.isEmpty())
			return s.isAvailable();
		return software(s);
	}
}
