/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories.windowcovering;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.accessories.GroupedAccessory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

/**
 * HomeKit WindowCovering accessory implemented with a GroupItem
 *
 * @author Andy Lintner
 */
class HomekitWindowCoveringGroupedImpl extends AbstractHomekitWindowCovering<GroupItem>implements GroupedAccessory {

    protected final String groupName;

    private final Logger logger = LoggerFactory.getLogger(HomekitWindowCoveringGroupedImpl.class);

    String positionItemName;
    private String obstructionDetectedItemName; // Optional

    public HomekitWindowCoveringGroupedImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
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
            case POSITION:
                this.positionItemName = item.getItem().getName();
                break;

            case OBSTRUCTION_DETECTED:
                this.obstructionDetectedItemName = item.getItem().getName();
                break;

            default:
                logger.error("Unrecognized window covering characteristic: " + item.getCharacteristicType().name());
                break;
        }
    }

    @Override
    public boolean isComplete() {
        return positionItemName != null;
    }

    @Override
    public CompletableFuture<Boolean> getObstructionDetected() {
        if (obstructionDetectedItemName == null) {
            return CompletableFuture.completedFuture(false);
        }
        Item item = getItemRegistry().get(obstructionDetectedItemName);
        OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(false);
        } else {
            return CompletableFuture.completedFuture(state == OnOffType.ON);
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
    protected String getPositionItemName() {
        return positionItemName;
    }

}
