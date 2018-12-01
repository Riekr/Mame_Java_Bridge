package com.riekr.mame.tools;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.Machines;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.beans.SoftwareLists;
import com.riekr.mame.config.ConfigFactory;
import com.riekr.mame.config.MameConfig;
import com.riekr.mame.utils.CacheFileManager;
import com.riekr.mame.utils.JaxbUtils;
import com.riekr.mame.utils.Sha1;
import com.riekr.mame.utils.Sync;
import com.riekr.mame.xmlsource.XmlSourceRef;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Mame implements Serializable {

	public static final ConfigFactory DEFAULT_CONFIG_FACTORY = new ConfigFactory();
	private static Mame _DEFAULT_INSTANCE;

	public static Mame getInstance() {
		Sync.dcInit(Mame.class, () -> _DEFAULT_INSTANCE == null,
				() -> _DEFAULT_INSTANCE = newInstance(DEFAULT_CONFIG_FACTORY));
		return _DEFAULT_INSTANCE;
	}

	@NotNull
	public static Mame newInstance(@NotNull Supplier<MameConfig> configSupplier) {
		return newInstance(configSupplier.get());
	}

	@NotNull
	public static Mame newInstance(@NotNull MameConfig config) {
		Mame res = CacheFileManager.loadCache(config.cacheFile);
		if (res == null)
			res = new Mame(config);
		res._config.check();
		return res;
	}

	public void invalidateCaches(boolean permanent) {
		if (_config.cacheFile != null) {
			if (permanent)
				CacheFileManager.invalidate(_config.cacheFile);
			else
				CacheFileManager.removeCache(_config.cacheFile);
		}
		_softwareLists = null;
		_machines = null;
	}

	private Mame(MameConfig config) {
		_config = config;
	}

	private MameConfig _config;
	private volatile SoftwareLists _softwareLists;
	private volatile Machines _machines;

	@NotNull
	public Set<Path> getRomPath() {
		return _config.romPath;
	}

	@NotNull
	public Set<Path> getSamplePath() {
		return _config.samplePath;
	}

	@NotNull
	public Stream<SoftwareList> softwareLists() {
		if (_config.softwaresXmlRef == null) {
			System.err.println("w: software lists not available.");
			return Stream.empty();
		}
		Sync.dcInit(this, () -> _softwareLists == null || _config.softwaresXmlRef.isOutDated(), () -> {
			try {
				try (InputStream is = _config.softwaresXmlRef.newInputStream(XmlSourceRef.Type.SOFTWARES)) {
					System.out.println("Parsing software lists...");
					_softwareLists = JaxbUtils.unmarshal(is, SoftwareLists.class);
				}
				_softwareLists.setParentNode(this);
				System.out.println("Got " + _softwareLists.count() + " software lists");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame software lists");
				e.printStackTrace(System.err);
			}
		});
		return _softwareLists.lists();
	}

	@NotNull
	public Stream<Machine> machines() {
		if (_config.machinesXmlRef == null) {
			System.err.println("w: machines not available.");
			return Stream.empty();
		}
		Sync.dcInit(this, () -> _machines == null || _config.machinesXmlRef.isOutDated(), () -> {
			try {
				try (InputStream is = _config.machinesXmlRef.newInputStream(XmlSourceRef.Type.MACHINES)) {
					System.out.println("Parsing machines...");
					_machines = JaxbUtils.unmarshal(is, Machines.class);
				}
				_machines.setParentNode(this);
				System.out.println("Got " + _machines.count() + " machines");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame machines");
				e.printStackTrace(System.err);
			}
		});
		return _machines.machines();
	}

	public String sha1(@NotNull Path file) {
		if (file.getFileName().toString().toLowerCase().endsWith(".chd")) {
			if (_config.chdManExec == null)
				throw new MameException("ChdMan executable not specified.");
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_config.chdManExec + " info -i \"" + file + '"');
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						// System.err.println(line);
						if (line.startsWith("SHA1:"))
							return line.substring(5).trim();
					}
				}
				return "";
			} catch (IOException e) {
				throw new MameException("Unable to create chd sha1 of " + file, e);
			}
		} else
			return Sha1.calc(file);
	}

	public void requestCachesWrite() {
		if (_config.cacheFile != null)
			CacheFileManager.register(_config.cacheFile, this);
	}
}
