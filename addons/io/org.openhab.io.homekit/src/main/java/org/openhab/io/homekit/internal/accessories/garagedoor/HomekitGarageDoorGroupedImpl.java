/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories.garagedoor;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.accessories.GroupedAccessory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

/**
 * HomeKit Garage Door implemented by a Group
 *
 * @author Andy Lintner
 */
public class HomekitGarageDoorGroupedImpl extends AbstractHomekitGarageDoor<GroupItem>implements GroupedAccessory {

    private final String groupName;
    private String obstructionDetectedItemName;
    String doorStateItemName;

    public HomekitGarageDoorGroupedImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, GroupItem.class);
        this.groupName = taggedItem.getItem().getName();
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        switch (item.getCharacteristicType()) {
            case DOOR_STATE:
                this.doorStateItemName = item.getItem().getName();
                break;

            case OBSTRUCTION_DETECTED:
                this.obstructionDetectedItemName = item.getItem().getName();
                break;

            default:
                logger.error("Unrecognized garage door characteristic: " + item.getCharacteristicType().name());
                break;
        }
    }

    @Override
    public boolean isComplete() {
        return doorStateItemName != null;
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        if (obstructionDetectedItemName == null) {
            return CompletableFuture.completedFuture(false);
        } else {
            Item item = getItemRegistry().get(obstructionDetectedItemName);
            OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
            if (state == null) {
                logger.warn("Could not get OnOffType from {}", obstructionDetectedItemName);
                return CompletableFuture.completedFuture(false);
            } else {
                return CompletableFuture.completedFuture(state == OnOffType.ON ? true : false);
            }
        }
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        if (obstructionDetectedItemName != null) {
            getUpdater().subscribe(getGenericItem(obstructionDetectedItemName), callback);
        }
    }

    @Override
    public void unsubscribeObstructionDetected() {
        if (obstructionDetectedItemName != null) {
            getUpdater().unsubscribe(getGenericItem(obstructionDetectedItemName));
        }
    }

    @Override
    protected String getDoorStateItemName() {
        return doorStateItemName;
    }

}
