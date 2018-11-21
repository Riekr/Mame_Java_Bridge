package com.riekr.mame.beans;

import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class Containers<ComponentType> {

	public final @NotNull ComponentType                        comp;
	public final @NotNull Set<Path>                            paths;
	private volatile      Collection<Container<ComponentType>> _unfolded;


	public Containers(@NotNull ComponentType comp, Set<Path> paths) {
		this.comp = comp;
		this.paths = Collections.unmodifiableSet(paths);
	}

	public Stream<Container<ComponentType>> unfold() {
		Sync.condInit(this, () -> _unfolded == null, () -> {
			if (paths.isEmpty())
				_unfolded = Collections.emptySet();
			else {
				_unfolded = new ArrayList<>(paths.size());
				for (Path path : paths)
					_unfolded.add(new Container<>(comp, path));
			}
		});
		return _unfolded.stream();
	}

	public boolean isEmpty() {
		return paths.isEmpty();
	}

	public ComponentType comp() {
		return comp;
	}

	public Set<Path> paths() {
		return paths;
	}
}
