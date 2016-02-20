/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories.characteristics;

import com.beowulfe.hap.accessories.GarageDoor;
import com.beowulfe.hap.accessories.properties.DoorState;

/**
 * Enum of possible door states when using a StringItem for a {@link GarageDoor}
 * 
 * @author Andy Lintner
 */
public enum OpenHabDoorState {

    OPEN("Open", DoorState.OPEN),
    CLOSED("Closed", DoorState.CLOSED),
    OPENING("Opening", DoorState.OPENING),
    CLOSING("Closing", DoorState.CLOSING),
    STOPPED("Stopped", DoorState.STOPPED);

    private final String stringValue;
    private final DoorState homekitState;

    private OpenHabDoorState(String stringValue, DoorState homekitState) {
        this.stringValue = stringValue;
        this.homekitState = homekitState;
    }

    public static OpenHabDoorState fromString(String stringValue) {
        for (OpenHabDoorState state : values()) {
            if (state.stringValue.equalsIgnoreCase(stringValue)) {
                return state;
            }
        }
        return OpenHabDoorState.STOPPED;
    }

    public static OpenHabDoorState fromHomekit(DoorState homekitState) {
        for (OpenHabDoorState state : values()) {
            if (state.homekitState == homekitState) {
                return state;
            }
        }
        return OpenHabDoorState.STOPPED;
    }

    public DoorState toHomekitState() {
        return homekitState;
    }

    public String getStringValue() {
        return stringValue;
    }
}
