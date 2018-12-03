package com.riekr.mame.beans;

import com.riekr.mame.attrs.MachineSearchResult;
import com.riekr.mame.attrs.RootList;
import com.riekr.mame.attrs.Searchable;
import com.riekr.mame.tools.Mame;
import com.riekr.mame.tools.MameXmlChildOf;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class Machines extends MameXmlChildOf<Mame> implements Serializable, RootList<Machine>, Searchable<MachineSearchResult> {

	@XmlAttribute
	public int mameconfig; // TODO check mameconfig version change

	@XmlElement(name = "machine")
	private List<Machine> _machines;

	private Map<String, MachineSearchResult> _search = new HashMap<>();

	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent) {
		all().forEach(m -> {
			_search.put(m.name, m);
			m.disks().filter(d -> d.sha1 != null).forEach(d -> _search.put(d.sha1, d));
			m.roms().forEach(r -> {
				if (r.sha1 != null)
					_search.put(r.sha1, r);
				if (r.crc != null)
					_search.put(r.crc, r);
			});
		});
	}

	@Override
	public int count() {
		return _machines == null ? 0 : _machines.size();
	}

	@Override
	@NotNull
	public Stream<Machine> all() {
		return _machines == null ? Stream.empty() : _machines.stream();
	}

	@Override
	@NotNull
	public Stream<MachineSearchResult> search(@NotNull Stream<String> keys) {
		return keys
				.filter(s -> s != null && !s.isBlank())
				.map(s -> _search.get(s));
	}
}
