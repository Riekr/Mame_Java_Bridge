package com.riekr.mame.xmlsource;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

class GZIPXmlSourceRef extends XmlSourceRef {

	GZIPXmlSourceRef(@NotNull File file) {
		super(file);
	}

	@Override
	public @NotNull InputStream newInputStream(@NotNull Type type) throws IOException {
		InputStream is = super.newInputStream(type);
		return new GZIPInputStream(is);
	}
}
