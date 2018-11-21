package com.riekr.mame.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SerUtils {

	private SerUtils() {
	}

	public static void writePath(@NotNull ObjectOutput out, @Nullable Path path) throws IOException {
		if (path == null)
			out.writeObject(null);
		else
			out.writeObject(path.toString());
	}

	public static Path readPath(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		Object o = in.readObject();
		if (o == null)
			return null;
		return Path.of((String) o);
	}

	public static void writePaths(@NotNull ObjectOutput out, @Nullable Collection<Path> pathSet) throws IOException {
		if (pathSet == null) {
			out.writeInt(-1);
			return;
		}
		out.writeInt(pathSet.size());
		for (Path p : pathSet)
			writePath(out, p);
	}

	public static Set<Path> readPathSet(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		final int sz = in.readInt();
		switch (sz) {
			case -1:
				return null;
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton(readPath(in));
		}
		HashSet<Path> res = new HashSet<>();
		for (int i = 0; i < sz; i++)
			res.add(readPath(in));
		return Collections.unmodifiableSet(res);
	}
}
