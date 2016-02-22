package org.openhab.io.homekit.internal.accessories.garagedoor;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.accessories.GarageDoor;

public class HomekitGarageDoorFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(HomekitGarageDoorFactory.class);

    public static GarageDoor create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        Item item = taggedItem.getItem();
        if (item instanceof GroupItem) {
            return new HomekitGarageDoorGroupedImpl(taggedItem, itemRegistry, updater);
        } else if (item instanceof RollershutterItem) {
            return new HomekitGarageDoorSimpleImpl(taggedItem, itemRegistry, updater);
        } else {
            LOGGER.error("Expected Group or Rollershutter type, found {} for item {}",
                    item.getClass().getCanonicalName(), item.getName());
            return null;
        }
    }
}
