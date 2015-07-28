package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;

public enum HomekitDeviceType {

	LIGHTBULB
	;
	
	private static final Map<String, HomekitDeviceType> lowerCaseMap = new HashMap<>();
	
	static {
		for (HomekitDeviceType type: HomekitDeviceType.values()) {
			lowerCaseMap.put(type.name().toLowerCase(), type);
		}
	}
	
	public static HomekitDeviceType valueOfCaseInsensitive(String type) {
		return lowerCaseMap.get(type.toLowerCase());
	}
}
