package com.riekr.mame.utils;

import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class MameXmlChildOf<ParentType> implements Serializable {

	private ParentType _parentNode;

	public void setParentNode(@NotNull ParentType parentNode) {
		_parentNode = parentNode;
	}

	@NotNull
	public final ParentType getParentNode() {
		return _parentNode;
	}

	public void notifyCachedDataChanged() {
		Mame.getInstance().requestCachesWrite();
	}
}
