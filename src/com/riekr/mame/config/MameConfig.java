package com.riekr.mame.config;

import com.riekr.mame.tools.MameException;
import com.riekr.mame.utils.SerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	public @NotNull Path mameExec;
	public @Nullable Path chdManExec;
	public @NotNull Set<Path> romPath;
	public @Nullable Path cacheFile;

	public MameConfig() {
	}

	public MameConfig(@NotNull Path mameExec, @Nullable Path chdManExec, @Nullable Set<Path> romPath, String id) {
		this.mameExec = mameExec;
		if (romPath == null || romPath.isEmpty())
			this.romPath = Collections.emptySet();
		else
			this.romPath = Collections.unmodifiableSet(romPath);
		if (id == null || id.isBlank())
			cacheFile = null;
		else
			cacheFile = Path.of(System.getProperty("user.home"), ".com.riekr.mame", id + ".cache");
		check();
		this.chdManExec = chdManExec;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MameConfig config = (MameConfig) o;
		return Objects.equals(mameExec, config.mameExec) && Objects.equals(romPath, config.romPath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mameExec, romPath);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerUtils.writePath(out, mameExec);
		SerUtils.writePaths(out, romPath);
		SerUtils.writePath(out, cacheFile);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		mameExec = Objects.requireNonNull(SerUtils.readPath(in));
		romPath = Objects.requireNonNull(SerUtils.readPathSet(in));
		cacheFile = SerUtils.readPath(in);
		check();
	}

	private void check() {
		//noinspection ConstantConditions
		if (mameExec == null)
			throw new MameException("Mame not specified.");
		if (!Files.exists(mameExec))
			throw new MameException("Specified mame executable not found: " + mameExec);
		if (!Files.isExecutable(mameExec))
			throw new MameException("Specified mame is not executable: " + mameExec);
	}
}
