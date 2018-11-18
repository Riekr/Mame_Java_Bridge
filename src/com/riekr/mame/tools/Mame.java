package com.riekr.mame.tools;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.Machines;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.beans.SoftwareLists;
import com.riekr.mame.config.ConfigFactory;
import com.riekr.mame.config.MameConfig;
import com.riekr.mame.utils.JaxbUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Mame implements Serializable {

	public static final ConfigFactory DEFAULT_CONFIG_FACTORY = new ConfigFactory();
	private static Mame _DEFAULT_INSTANCE;

	public static Mame getInstance() {
		if (_DEFAULT_INSTANCE == null)
			_DEFAULT_INSTANCE = newInstance(DEFAULT_CONFIG_FACTORY);
		return _DEFAULT_INSTANCE;
	}

	@NotNull
	private static Mame prepare(@NotNull Mame newInstance) {
		newInstance._execLastModified = newInstance._config.exec.lastModified();
		newInstance.requestCachesWrite();
		return newInstance;
	}

	@NotNull
	public static Mame newInstance(@NotNull Supplier<MameConfig> configSupplier) {
		return newInstance(configSupplier.get());
	}

	@NotNull
	public static Mame newInstance(@NotNull MameConfig config) {
		Mame res = loadFromCaches(config.cacheFile);
		if (res == null)
			res = prepare(new Mame(config));
		else {
			if (res._execLastModified != res._config.exec.lastModified()) {
				Mame newInstance = new Mame(res._config);
				if (!newInstance.version().equals(res._version))
					res = prepare(newInstance);
				else
					prepare(res);
			}
		}
		return res;
	}

	private static Mame loadFromCaches(File cacheFile) {
		if (cacheFile == null)
			return null;
		try {
			if (cacheFile.canRead()) {
				System.out.println("Loading caches from " + cacheFile);
				Mame mame;
				try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(cacheFile)))) {
					mame = (Mame) ois.readObject();
				}
				if (mame != null) {
					System.out.println("Restored cache for version " + mame._version);
					return mame;
				}
			}
		} catch (InvalidClassException e) {
			System.err.println("Cache format changed, data invalidated.");
		} catch (Exception e) {
			System.err.println("Unable to read " + cacheFile);
			e.printStackTrace(System.err);
		}
		return null;
	}

	public void flushCaches() {
		if (_config.cacheFile == null)
			return;
		System.out.println("Writing caches to " + _config.cacheFile);
		try {
			//noinspection ResultOfMethodCallIgnored
			_config.cacheFile.getParentFile().mkdirs();
			try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(_config.cacheFile, false)))) {
				oos.writeObject(this);
			}
		} catch (Exception e) {
			System.err.println("Unable to write " + _config.cacheFile);
			e.printStackTrace(System.err);
		}
	}

	public void invalidateCaches() {
		if (_config.cacheFile != null)
			_config.cacheFile.deleteOnExit();
		_version = null;
		_softwareLists = null;
	}

	private Mame(MameConfig config) {
		_config = config;
	}

	private MameConfig _config;
	private SoftwareLists _softwareLists;
	private Machines _machines;
	private long _execLastModified;
	private String _version;
	private transient boolean _writeCacheRequested = false;

	@NotNull
	public Set<File> getRomPath() {
		return _config.romPath;
	}

	@NotNull
	public Stream<SoftwareList> softwareLists() {
		if (_softwareLists == null) {
			try {
				System.out.println("Getting software lists for v" + version());
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_config.exec + " -getsoftlist", null, _config.exec.getParentFile());
				byte[] buf;
				try (InputStream is = proc.getInputStream()) {
					buf = is.readAllBytes();
				}
				System.out.println("Parsing software lists (" + buf.length + " bytes)");
				_softwareLists = JaxbUtils.unmarshal(buf, SoftwareLists.class);
				_softwareLists.setParentNode(this);
				System.out.println("Got " + _softwareLists.lists.size() + " software lists");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame software lists");
				e.printStackTrace(System.err);
			}
		}
		return _softwareLists.lists == null ? Stream.empty() : _softwareLists.lists.stream();
	}

	@NotNull
	public Stream<Machine> machines() {
		if (_machines == null) {
			try {
				System.out.println("Getting machines for v" + version());
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_config.exec + " -listxml", null, _config.exec.getParentFile());
				byte[] buf;
				try (InputStream is = proc.getInputStream()) {
					buf = is.readAllBytes();
				}
				System.out.println("Parsing machines (" + buf.length + " bytes)");
				_machines = JaxbUtils.unmarshal(buf, Machines.class);
				_machines.setParentNode(this);
				System.out.println("Got " + _machines.machines.size() + " machines");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame machines");
				e.printStackTrace(System.err);
			}
		}
		return _machines.machines == null ? Stream.empty() : _machines.machines.stream();
	}

	@NotNull
	public String version() {
		if (_version == null) {
			_version = "<undef>";
			try {
				System.out.println("Getting mame version from " + _config.exec);
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_config.exec + " -help", null, _config.exec.getParentFile());
				// MAME v0.203 (mame0203)
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
					Matcher m = Pattern.compile("MAME\\s+v([0-9.]+)\\s+.*").matcher("");
					String line;
					while ((line = reader.readLine()) != null) {
						m.reset(line);
						if (m.matches()) {
							_version = m.group(1);
							break;
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Unable to get mame version");
				e.printStackTrace(System.err);
			}
		}
		return _version;
	}

	public void requestCachesWrite() {
		if (!_writeCacheRequested) {
			_writeCacheRequested = true;
			if (_config.cacheFile != null)
				Runtime.getRuntime().addShutdownHook(new Thread(this::flushCaches));
		}
	}

	@Override
	public String toString() {
		return _config.exec.getName() + ' ' + _version;
	}
}
