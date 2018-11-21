package com.riekr.mame.tools;

import com.riekr.mame.beans.Machine;
import com.riekr.mame.beans.Machines;
import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.beans.SoftwareLists;
import com.riekr.mame.config.ConfigFactory;
import com.riekr.mame.config.MameConfig;
import com.riekr.mame.utils.JaxbUtils;
import com.riekr.mame.utils.Sha1;
import com.riekr.mame.utils.Sync;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class Mame implements Serializable {

	public static final ConfigFactory DEFAULT_CONFIG_FACTORY = new ConfigFactory();
	private static      Mame          _DEFAULT_INSTANCE;

	public static Mame getInstance() {
		Sync.condInit(Mame.class, () -> _DEFAULT_INSTANCE == null,
				() -> _DEFAULT_INSTANCE = newInstance(DEFAULT_CONFIG_FACTORY));
		return _DEFAULT_INSTANCE;
	}

	@NotNull
	private static Mame prepare(@NotNull Mame newInstance) {
		newInstance._execLastModified = newInstance._config.mameExec.toFile().lastModified();
		newInstance.requestCachesWrite();
		return newInstance;
	}

	@NotNull
	public static Mame newInstance(@NotNull Supplier<MameConfig> configSupplier) {
		return newInstance(configSupplier.get());
	}

	@NotNull
	public static Mame newInstance(@NotNull MameConfig config) {
		Mame res = loadFromCache(config.cacheFile);
		if (res == null)
			res = prepare(new Mame(config));
		else {
			if (res._execLastModified != res._config.mameExec.toFile().lastModified()) {
				Mame newInstance = new Mame(res._config);
				if (!newInstance.version().equals(res._version))
					res = prepare(newInstance);
				else
					prepare(res);
			}
		}
		return res;
	}

	@Nullable
	private static Mame loadFromCache(@Nullable Path cacheFile) {
		if (cacheFile == null || !Files.isReadable(cacheFile))
			return null;
		try {
			System.out.println("Loading caches from " + cacheFile);
			Mame mame;
			try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(Files.newInputStream(cacheFile)))) {
				mame = (Mame) ois.readObject();
			}
			if (mame != null) {
				mame._writeCacheRequested = new AtomicBoolean(false);
				System.out.println("Restored cache for version " + mame._version);
				return mame;
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
		if (_writeCacheRequested.compareAndSet(true, false)) {
			synchronized (_config.cacheFile) {
				System.out.println("Writing caches to " + _config.cacheFile);
				try {
					Files.createDirectories(_config.cacheFile.getParent());
					try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(_config.cacheFile, CREATE_NEW)))) {
						oos.writeObject(this);
					}
				} catch (Exception e) {
					System.err.println("Unable to write " + _config.cacheFile);
					e.printStackTrace(System.err);
				}
			}
		}
	}

	public void invalidateCaches() {
		if (_config.cacheFile != null)
			_config.cacheFile.toFile().deleteOnExit();
		_version = null;
		_softwareLists = null;
	}

	private Mame(MameConfig config) {
		_config = config;
	}

	private           MameConfig    _config;
	private volatile  SoftwareLists _softwareLists;
	private volatile  Machines      _machines;
	private volatile  String        _version;
	private           long          _execLastModified;
	private transient AtomicBoolean _writeCacheRequested = new AtomicBoolean(false);

	@NotNull
	public Set<Path> getRomPath() {
		return _config.romPath;
	}

	@NotNull
	public Stream<SoftwareList> softwareLists() {
		Sync.condInit(this, () -> _softwareLists == null, () -> {
			try {
				System.out.println("Getting software lists for v" + version());
				Runtime rt = Runtime.getRuntime();
				File home = _config.mameExec.getParent().toFile();
				Process proc = rt.exec(_config.mameExec + " -getsoftlist", null, home);
				System.out.println("Parsing software lists...");
				try (InputStream is = proc.getInputStream()) {
					_softwareLists = JaxbUtils.unmarshal(is, SoftwareLists.class);
				}
				_softwareLists.setParentNode(this);
				System.out.println("Got " + _softwareLists.lists.size() + " software lists");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame software lists");
				e.printStackTrace(System.err);
			}
		});
		return _softwareLists.lists == null ? Stream.empty() : _softwareLists.lists.stream();
	}

	@NotNull
	public Stream<Machine> machines() {
		Sync.condInit(this, () -> _machines == null, () -> {
			try {
				System.out.println("Getting machines for v" + version());
				Runtime rt = Runtime.getRuntime();
				File home = _config.mameExec.getParent().toFile();
				Process proc = rt.exec(_config.mameExec + " -listxml", null, home);
				System.out.println("Parsing machines...");
				try (InputStream is = proc.getInputStream()) {
					_machines = JaxbUtils.unmarshal(is, Machines.class);
				}
				_machines.setParentNode(this);
				System.out.println("Got " + _machines.machines.size() + " machines");
				requestCachesWrite();
			} catch (Exception e) {
				System.err.println("Unable to get mame machines");
				e.printStackTrace(System.err);
			}
		});
		return _machines.machines == null ? Stream.empty() : _machines.machines.stream();
	}

	@NotNull
	public String version() {
		Sync.condInit(this, () -> _version == null, () -> {
			_version = "<undef>";
			try {
				System.out.println("Getting mame version from " + _config.mameExec);
				Runtime rt = Runtime.getRuntime();
				File home = _config.mameExec.getParent().toFile();
				Process proc = rt.exec(_config.mameExec + " -help", null, home);
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
		});
		return _version;
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
		if (_writeCacheRequested.compareAndSet(false, true)) {
			if (_config.cacheFile != null)
				Runtime.getRuntime().addShutdownHook(new Thread(this::flushCaches));
		}
	}

	@Override
	public String toString() {
		return _config.mameExec.getFileName() + " " + _version;
	}
}
