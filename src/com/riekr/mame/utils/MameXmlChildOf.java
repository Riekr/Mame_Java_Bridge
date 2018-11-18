package com.riekr.mame.utils;

import com.riekr.mame.tools.Mame;
import com.riekr.mame.tools.MameException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class MameXmlChildOf<ParentType extends Object & Serializable> implements Serializable {

	private ParentType _parentNode;

	public void setParentNode(@NotNull ParentType parentNode) {
		_parentNode = parentNode;
	}

	@NotNull
	public final ParentType getParentNode() {
		return _parentNode;
	}

	public Mame getMame() {
		if (_parentNode instanceof Mame)
			return (Mame) _parentNode;
		if (_parentNode instanceof MameXmlChildOf<?>)
			return ((MameXmlChildOf<?>) _parentNode).getMame();
		throw new MameException("Unsupported parent node in " + getClass().getName() + " (" + (_parentNode == null ? "null" : _parentNode.getClass().getName() + ')'));
	}

	public void notifyCachedDataChanged() {
		getMame().requestCachesWrite();
	}
}
