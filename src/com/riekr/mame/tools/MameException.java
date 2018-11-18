package com.riekr.mame.tools;

public class MameException extends RuntimeException {

	public MameException(String message) {
		super(message);
	}

	public MameException(String message, Throwable reason) {
		super(message, reason);
	}
}
