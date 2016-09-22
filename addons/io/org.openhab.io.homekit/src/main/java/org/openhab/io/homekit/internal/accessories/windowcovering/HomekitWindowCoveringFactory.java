/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories.windowcovering;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.accessories.WindowCovering;

/**
 * Factory providing HomekitWindowCovering instances
 *
 * @author Andy Lintner
 */
public class HomekitWindowCoveringFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(HomekitWindowCoveringFactory.class);

    public static WindowCovering create(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        Item item = taggedItem.getItem();
        if (item instanceof GroupItem) {
            boolean hasVerticalTilt = false;
            boolean hasHorizontalTilt = false;
            for (Item groupItem : ((GroupItem) item).getMembers()) {
                if (HomekitTaggedItem.isTagged(groupItem)) {
                    if (new HomekitTaggedItem(groupItem, itemRegistry)
                            .getCharacteristicType() == HomekitCharacteristicType.HORIZONTAL_TILT_ANGLE) {
                        hasHorizontalTilt = true;
                    } else if (new HomekitTaggedItem(groupItem, itemRegistry)
                            .getCharacteristicType() == HomekitCharacteristicType.VERTICAL_TILT_ANGLE) {
                        hasVerticalTilt = true;
                    }
                }
            }
            if (hasHorizontalTilt && hasVerticalTilt) {
                LOGGER.error("WindowCovering {} cannot contain both horizontal and vertical tilt", item.getName());
                return null;
            } else if (hasHorizontalTilt) {
                return new HomekitHorizontalTiltingWindowCoveringImpl(taggedItem, itemRegistry, updater);
            } else if (hasVerticalTilt) {
                return new HomekitVerticalTiltingWindowCoveringImpl(taggedItem, itemRegistry, updater);
            } else {
                return new HomekitWindowCoveringGroupedImpl(taggedItem, itemRegistry, updater);
            }
        } else if (item instanceof RollershutterItem) {
            return new HomekitWindowCoveringSimpleImpl(taggedItem, itemRegistry, updater);
        } else {
            LOGGER.error("Expected Group or Rollershutter type, found {} for item {}",
                    item.getClass().getCanonicalName(), item.getName());
            return null;
        }
    }
}
