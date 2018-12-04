package com.riekr.mame.tools;

import com.riekr.mame.attrs.RootList;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.Unmarshaller;
import java.io.Serializable;

public class MameXmlChildOf<ParentType extends Object & Serializable> implements Serializable {

	static class UnmarshalListener extends Unmarshaller.Listener {
		private final @NotNull Mame _mame;

		public UnmarshalListener(@NotNull Mame mame) {
			_mame = mame;
		}

		@Override
		public void afterUnmarshal(Object target, Object parent) {
			if (target instanceof MameXmlChildOf) {
				if (target instanceof RootList)
					((MameXmlChildOf) target)._parentNode = _mame;
				else
					((MameXmlChildOf) target)._parentNode = parent;
			}
		}
	}

	@SuppressWarnings("NullableProblems")
	private @NotNull ParentType _parentNode;

	@NotNull
	protected final ParentType getParentNode() {
		return _parentNode;
	}

	public final Mame getMame() {
		if (_parentNode instanceof Mame)
			return (Mame) _parentNode;
		return ((MameXmlChildOf<?>) _parentNode).getMame();
	}

	public void notifyCachedDataChanged() {
		getMame().requestCachesWrite();
	}
}
