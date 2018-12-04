package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.SerUtils;
import com.riekr.mame.xmlsource.XmlSourceRef;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class MameConfig implements Externalizable {

	public Path         mameExec;
	public Path         chdManExec;
	public Set<Path>    romPath    = Collections.emptySet();
	public Set<Path>    samplePath = Collections.emptySet();
	public Path         cacheFile;
	public XmlSourceRef machinesXmlRef;
	public XmlSourceRef softwaresXmlRef;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MameConfig config = (MameConfig) o;
		return Objects.equals(mameExec, config.mameExec)
				&& Objects.equals(chdManExec, config.chdManExec)
				&& Objects.equals(romPath, config.romPath)
				&& Objects.equals(samplePath, config.samplePath)
				&& Objects.equals(machinesXmlRef, config.machinesXmlRef)
				&& Objects.equals(softwaresXmlRef, config.softwaresXmlRef);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mameExec, chdManExec, romPath, samplePath, machinesXmlRef, softwaresXmlRef);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerUtils.writePath(out, mameExec);
		SerUtils.writePath(out, chdManExec);
		SerUtils.writePaths(out, romPath);
		SerUtils.writePaths(out, samplePath);
		SerUtils.writePath(out, cacheFile);
		out.writeObject(machinesXmlRef);
		out.writeObject(softwaresXmlRef);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		mameExec = SerUtils.readPath(in);
		chdManExec = SerUtils.readPath(in);
		romPath = Collections.unmodifiableSet(Objects.requireNonNull(SerUtils.readPathSet(in)));
		samplePath = Collections.unmodifiableSet(Objects.requireNonNull(SerUtils.readPathSet(in)));
		cacheFile = SerUtils.readPath(in);
		machinesXmlRef = (XmlSourceRef) in.readObject();
		softwaresXmlRef = (XmlSourceRef) in.readObject();
	}

	public void check() {
		if (machinesXmlRef == null && softwaresXmlRef == null) {
			if (mameExec == null)
				throw new MameException("Mame not specified.");
			if (!Files.exists(mameExec))
				throw new MameException("Specified mame executable not found: " + mameExec);
			if (!Files.isExecutable(mameExec))
				throw new MameException("Specified mame is not executable: " + mameExec);
		}
		if (mameExec != null && (machinesXmlRef == null || softwaresXmlRef == null)) {
			XmlSourceRef mameSourceRef = XmlSourceRef.from(mameExec);
			if (machinesXmlRef == null)
				machinesXmlRef = mameSourceRef;
			if (softwaresXmlRef == null)
				softwaresXmlRef = mameSourceRef;
		}
		if (machinesXmlRef == null && softwaresXmlRef == null)
			throw new MameException("No xml sources specified");
		if (romPath == null)
			this.romPath = Collections.emptySet();
		if (samplePath == null)
			this.samplePath = Collections.emptySet();
	}
}
