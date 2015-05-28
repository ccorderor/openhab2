package com.beowulfe.openhab.homekit.internal;

import java.util.HashMap;
import java.util.Map;

public enum HomekitDeviceType {

	SWITCH("Switch")
	;
	
	private static final Map<String, HomekitDeviceType> openhabTypeMap = new HashMap<>();
	
	static {
		for (HomekitDeviceType type: HomekitDeviceType.values()) {
			openhabTypeMap.put(type.openhabType, type);
		}
	}
	
	private final String openhabType;
	
	HomekitDeviceType(String openhabType) {
		this.openhabType = openhabType;
	}
	
	public static boolean supportsOpenhabType(String type) {
		return openhabTypeMap.containsKey(type);
	}
}
