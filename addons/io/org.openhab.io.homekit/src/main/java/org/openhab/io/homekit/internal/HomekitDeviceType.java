package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;

public enum HomekitDeviceType {

    LIGHTBULB("Lightbulb"),
    THERMOSTAT("Thermostat");

    private static final Map<String, HomekitDeviceType> tagMap = new HashMap<>();

    static {
        for (HomekitDeviceType type : HomekitDeviceType.values()) {
            tagMap.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitDeviceType(String tag) {
        this.tag = tag;
    }

    public static HomekitDeviceType valueOfTag(String tag) {
        return tagMap.get(tag);
    }
}
