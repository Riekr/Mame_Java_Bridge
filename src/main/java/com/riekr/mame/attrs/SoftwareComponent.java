package com.riekr.mame.attrs;

import com.riekr.mame.beans.Software;
import org.jetbrains.annotations.NotNull;

public interface SoftwareComponent {

	@NotNull
	Software getSoftware();

}
