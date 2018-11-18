package com.riekr.mame.utils;

import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

	private static final HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();

	private Sha1() {
	}

	@NotNull
	public static String calc(@NotNull File file) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			try (InputStream input = new FileInputStream(file)) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = input.read(buffer)) != -1)
					sha1.update(buffer, 0, len);
				return hexBinaryAdapter.marshal(sha1.digest());
			}
		} catch (IOException | NoSuchAlgorithmException e) {
			throw new MameException("Unable to calculate sha1 of " + file, e);
		}
	}
}
