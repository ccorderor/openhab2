/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.accessories.characteristics.OpenHabDoorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.GarageDoor;
import com.beowulfe.hap.accessories.properties.DoorState;

/**
 * openHAB impelementation of HomeKit GarageDoor accessory
 *
 * @author Andy Lintner
 */
public class HomekitGarageDoorImpl extends AbstractHomekitAccessoryImpl<GroupItem>
        implements GarageDoor, GroupedAccessory {

    private Logger logger = LoggerFactory.getLogger(HomekitGarageDoorImpl.class);
    private final String groupName;
    private String obstructionDetectedItemName;
    private String doorStateItemName;

    public HomekitGarageDoorImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
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
    public CompletableFuture<DoorState> getCurrentDoorState() {
        Item item = getItemRegistry().get(doorStateItemName);
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state != null) {
            return CompletableFuture.completedFuture(targetStateFromPercentType(state));
        } else {
            return CompletableFuture.completedFuture(getGranularState());
        }
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
    public CompletableFuture<DoorState> getTargetDoorState() {
        return getCurrentDoorState().thenApply(currentState -> {
            switch (currentState) {
                case CLOSING:
                case CLOSED:
                    return DoorState.CLOSED;

                default:
                    return DoorState.OPEN;
            }

        });
    }

    @Override
    public CompletableFuture<Void> setTargetDoorState(DoorState state) throws Exception {
        GenericItem item = getGenericItem(doorStateItemName);
        if (item instanceof RollershutterItem) {
            ((RollershutterItem) item).send(state == DoorState.OPEN ? UpDownType.UP : UpDownType.DOWN);
        } else if (item instanceof StringItem) {
            ((StringItem) item).send(new StringType(OpenHabDoorState.fromHomekit(state).getStringValue()));
        } else {
            logger.error("{} must be a RollershutterItem or StringItem");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentDoorState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(doorStateItemName), "currentState", callback);
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        if (obstructionDetectedItemName != null) {
            getUpdater().subscribe(getGenericItem(obstructionDetectedItemName), callback);
        }
    }

    @Override
    public void subscribeTargetDoorState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(doorStateItemName), "targetState", (oldState, newState) -> {
            if (newState instanceof PercentType) {
                if (!targetStateFromPercentType((PercentType) newState)
                        .equals(targetStateFromPercentType((PercentType) oldState))) {
                    callback.changed();
                }
                // Target state for a PercentType collapses several current states to a single state. Only fire callback
                // if target state changed.
            } else {
                callback.changed();
            }
        });
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        getUpdater().unsubscribe(getGenericItem("doorStateItemname"), "currentState");
    }

    @Override
    public void unsubscribeObstructionDetected() {
        if (obstructionDetectedItemName != null) {
            getUpdater().unsubscribe(getGenericItem(obstructionDetectedItemName));
        }
    }

    @Override
    public void unsubscribeTargetDoorState() {
        getUpdater().unsubscribe(getGenericItem("doorStateItemname"), "targetState");
    }

    private DoorState targetStateFromPercentType(PercentType state) {
        if (state.intValue() == 100) {
            return DoorState.CLOSED;
        } else {
            return DoorState.OPEN;
        }
    }

    private DoorState getGranularState() {
        Item stateItem = getItemRegistry().get(doorStateItemName);
        StringType stringState = (StringType) stateItem.getStateAs(StringType.class);
        if (stringState == null) {
            logger.warn("Could not get StringType from {}", doorStateItemName);
            return DoorState.OPEN;
        } else {
            return OpenHabDoorState.fromString(stringState.toString()).toHomekitState();
        }
    }

}
