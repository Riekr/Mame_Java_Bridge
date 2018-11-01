package com.riekr.mame.utils;

import com.riekr.mame.tools.Mame;
import org.jetbrains.annotations.NotNull;

public class MameXmlChildOf<ParentType> {

	private transient ParentType _parentNode;

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
