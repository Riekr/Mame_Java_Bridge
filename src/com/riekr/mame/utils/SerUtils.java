package com.riekr.mame.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.*;

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

	public static void writePaths(@NotNull ObjectOutput out, @Nullable Map<Path, ?> pathMap) throws IOException {
		if (pathMap == null) {
			out.writeInt(-1);
			return;
		}
		out.writeInt(pathMap.size());
		for (Map.Entry<Path, ?> e : pathMap.entrySet()) {
			writePath(out, e.getKey());
			out.writeObject(e.getValue());
		}
	}

	public static Set<Path> readPathSet(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		final int sz = in.readInt();
		if (sz < 0)
			return null;
		HashSet<Path> res = new HashSet<>(sz);
		for (int i = 0; i < sz; i++)
			res.add(readPath(in));
		return res;
	}

	public static <T> Map<Path, T> readPathMap(@NotNull ObjectInput in) throws IOException, ClassNotFoundException {
		final int sz = in.readInt();
		if (sz < 0)
			return null;
		HashMap<Path, T> res = new HashMap<>(sz);
		for (int i = 0; i < sz; i++)
			//noinspection unchecked
			res.put(readPath(in), (T) in.readObject());
		return res;
	}
}
