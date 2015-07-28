package org.openhab.io.homekit;

import org.eclipse.smarthome.core.types.State;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

public interface HomekitAccessoryUpdater {

	void unsubscribe(String itemName, Class<? extends State> type);

	void subscribe(String itemName, Class<? extends State> type,
			HomekitCharacteristicChangeCallback callback);

}
