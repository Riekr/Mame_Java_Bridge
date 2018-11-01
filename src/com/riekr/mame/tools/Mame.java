package com.riekr.mame.tools;

import com.riekr.mame.beans.SoftwareList;
import com.riekr.mame.beans.SoftwareLists;
import com.riekr.mame.utils.JaxbUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Mame implements Serializable {

	private static Mame _instance;
	private static final File _exec;
	private static final Set<File> _romPath = new HashSet<>();
	private static final File _settingsDir = new File(System.getProperty("user.home"), ".com.riekr.mame");
	private static final File _mameCache = new File(_settingsDir, "Mame.cache");

	static {
		File mameRoot = new File("D:\\Giochi\\Mame");
		_exec = new File(mameRoot, "mame64.exe");
		for (String s : new String[]{"roms", "SL", "CHD"})
			_romPath.add(new File(mameRoot, s));
	}

	@NotNull
	private static Mame prepare(@NotNull Mame newInstance) {
		newInstance._execLastModified = _exec.lastModified();
		newInstance.requestCachesWrite();
		return newInstance;
	}

	@NotNull
	public static Mame getInstance() {
		if (_instance == null) {
			_instance = loadFromCaches();
			if (_instance == null)
				_instance = prepare(new Mame());
			else if (_instance._execLastModified != _exec.lastModified()) {
				Mame newInstance = new Mame();
				if (!newInstance.version().equals(_instance._version))
					_instance = prepare(newInstance);
				else
					prepare(_instance);
			}
		}
		return _instance;
	}

	private static Mame loadFromCaches() {
		try {
			if (_mameCache.canRead()) {
				System.out.println("Loading caches from " + _mameCache);
				Mame mame;
				try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(_mameCache)))) {
					mame = (Mame) ois.readObject();
				}
				if (mame != null) {
					if (mame._softwareLists != null)
						mame._softwareLists.setParentNode(mame);
					System.out.println("Restored cache for version " + mame._version);
					return mame;
				}
			}
		} catch (InvalidClassException e) {
			System.err.println("Cache format changed, data invalidated.");
		} catch (Exception e) {
			System.err.println("Unable to read " + _mameCache);
			e.printStackTrace(System.err);
		}
		return null;
	}

	public void flushCaches() {
		System.out.println("Writing caches to " + _mameCache);
		try {
			//noinspection ResultOfMethodCallIgnored
			_mameCache.getParentFile().mkdirs();
			try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(_mameCache, false)))) {
				oos.writeObject(this);
			}
		} catch (Exception e) {
			System.err.println("Unable to write " + _mameCache);
			e.printStackTrace(System.err);
		}
	}

	public void invalidateCaches() {
		_mameCache.deleteOnExit();
		_version = null;
		_softwareLists = null;
	}


	private SoftwareLists _softwareLists;
	private long _execLastModified;
	private String _version;
	private transient boolean _writeCacheRequested = false;

	@NotNull
	public Set<File> getRomPath() {
		return _romPath;
	}

	@NotNull
	public Stream<SoftwareList> softwareLists() {
		if (_softwareLists == null) {
			try {
				System.out.println("Getting software lists for v" + version());
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_exec + " -getsoftlist", null, _exec.getParentFile());
				byte[] buf;
				try (InputStream is = proc.getInputStream()) {
					buf = is.readAllBytes();
				}
				System.out.println("Parsing");
				_softwareLists = JaxbUtils.unmarshal(buf, SoftwareLists.class);
				_softwareLists.setParentNode(this);
				System.out.println("Got " + _softwareLists.lists.size() + " lists");
			} catch (Exception e) {
				System.err.println("Unable to get mame software lists");
				e.printStackTrace(System.err);
			}
		}
		return _softwareLists.lists == null ? Stream.empty() : _softwareLists.lists.stream();
	}

	@NotNull
	public String version() {
		if (_version == null) {
			_version = "<undef>";
			try {
				System.out.println("Getting mame version");
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(_exec + " -help", null, _exec.getParentFile());
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

	private Mame() {
	}

	public void requestCachesWrite() {
		if (!_writeCacheRequested) {
			_writeCacheRequested = true;
			Runtime.getRuntime().addShutdownHook(new Thread(() -> _instance.flushCaches()));
		}
	}
}
