package com.riekr.mame.utils;

import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class PrintStreamTee extends PrintStream {

	public static PrintStream to(@Nullable Path f) {
		if (f == null)
			return System.out;
		try {
			PrintStreamTee tee = new PrintStreamTee(new BufferedOutputStream(Files.newOutputStream(f, CREATE, TRUNCATE_EXISTING)));
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				tee.flush();
				tee.close();
			}));
			return tee;
		} catch (IOException e) {
			throw new MameException("Unable to tee std output to " + f, e);
		}
	}

	public PrintStreamTee(@NotNull OutputStream out) {
		super(out);
	}

	public PrintStreamTee(@NotNull OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public PrintStreamTee(@NotNull OutputStream out, boolean autoFlush, @NotNull String encoding) throws UnsupportedEncodingException {
		super(out, autoFlush, encoding);
	}

	public PrintStreamTee(OutputStream out, boolean autoFlush, Charset charset) {
		super(out, autoFlush, charset);
	}

	public PrintStreamTee(@NotNull String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public PrintStreamTee(@NotNull String fileName, @NotNull String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
	}

	public PrintStreamTee(String fileName, Charset charset) throws IOException {
		super(fileName, charset);
	}

	public PrintStreamTee(@NotNull File file) throws FileNotFoundException {
		super(file);
	}

	public PrintStreamTee(@NotNull File file, @NotNull String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
	}

	public PrintStreamTee(File file, Charset charset) throws IOException {
		super(file, charset);
	}

	@Override
	public void flush() {
		super.flush();
		System.out.flush();
	}


	@Override
	public void print(boolean b) {
		super.print(b);
		System.out.print(b);
	}

	@Override
	public void print(char c) {
		super.print(c);
		System.out.print(c);
	}

	@Override
	public void print(int i) {
		super.print(i);
		System.out.print(i);
	}

	@Override
	public void print(long l) {
		super.print(l);
		System.out.print(l);
	}

	@Override
	public void print(float f) {
		super.print(f);
		System.out.print(f);
	}

	@Override
	public void print(double d) {
		super.print(d);
		System.out.print(d);
	}

	@Override
	public void print(@NotNull char[] s) {
		super.print(s);
		System.out.print(s);
	}

	@Override
	public void print(String s) {
		super.print(s);
		System.out.print(s);
	}

	@Override
	public void print(Object obj) {
		super.print(obj);
		System.out.print(obj);
	}

	@Override
	public void println() {
		super.println();
		System.out.println();
	}

	@Override
	public void println(boolean x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(char x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(int x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(long x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(float x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(double x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(@NotNull char[] x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(String x) {
		super.println(x);
		System.out.println(x);
	}

	@Override
	public void println(Object x) {
		super.println(x);
		System.out.println(x);
	}
}
