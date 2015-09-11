package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

public class HomekitAccessoryFactory {

    public static HomekitAccessory create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings) throws Exception {
        switch (taggedItem.getDeviceType()) {
            case LIGHTBULB:
                return new HomekitLightbulbImpl(taggedItem, itemRegistry, updater);

            case DIMMABLE_LIGHTBULB:
                return new HomekitDimmableLightbulbImpl(taggedItem, itemRegistry, updater);

            case THERMOSTAT:
                return new HomekitThermostatImpl(taggedItem, itemRegistry, updater, settings);

            default:
                throw new Exception("Unknown homekit type: " + taggedItem.getDeviceType());
        }
    }
}
