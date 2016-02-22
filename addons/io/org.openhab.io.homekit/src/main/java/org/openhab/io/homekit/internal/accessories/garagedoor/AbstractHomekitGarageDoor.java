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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.accessories.AbstractHomekitAccessoryImpl;
import org.openhab.io.homekit.internal.accessories.characteristics.OpenHabDoorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.GarageDoor;
import com.beowulfe.hap.accessories.properties.DoorState;

/**
 * Abstract implementation of HomeKit Garage Door accessory
 *
 * @author Andy Lintner
 */
public abstract class AbstractHomekitGarageDoor<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T>
        implements GarageDoor {

    protected Logger logger = LoggerFactory.getLogger(HomekitGarageDoorGroupedImpl.class);

    public AbstractHomekitGarageDoor(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
    }

    @Override
    public CompletableFuture<DoorState> getCurrentDoorState() {
        Item item = getItemRegistry().get(getDoorStateItemName());
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state != null) {
            return CompletableFuture.completedFuture(booleanStateFromPercentType(state));
        } else {
            return CompletableFuture.completedFuture(getGranularState());
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
        GenericItem item = getGenericItem(getDoorStateItemName());
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
        getUpdater().subscribe(getGenericItem(getDoorStateItemName()), "currentState", callback);
    }

    @Override
    public void subscribeTargetDoorState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(getDoorStateItemName()), "targetState", (oldState, newState) -> {
            if (newState instanceof PercentType) {
                if (!booleanStateFromPercentType((PercentType) newState)
                        .equals(booleanStateFromPercentType((PercentType) oldState))) {
                    callback.changed();
                }
                // Target state for a PercentType collapses several current states to a single state. Only fire callback
                // if target state changed.
            } else {
                if (!booleanStateFromEnumType((StringType) newState)
                        .equals(booleanStateFromEnumType((StringType) oldState))) {
                    callback.changed();
                }
            }
        });
    }

    @Override
    public void unsubscribeCurrentDoorState() {
        getUpdater().unsubscribe(getGenericItem("doorStateItemname"), "currentState");
    }

    @Override
    public void unsubscribeTargetDoorState() {
        getUpdater().unsubscribe(getGenericItem("doorStateItemname"), "targetState");
    }

    protected abstract String getDoorStateItemName();

    private DoorState booleanStateFromPercentType(PercentType state) {
        if (state.intValue() == 100) {
            return DoorState.CLOSED;
        } else {
            return DoorState.OPEN;
        }
    }

    private DoorState booleanStateFromEnumType(StringType state) {
        switch (OpenHabDoorState.fromString(state.toString())) {
            case CLOSED:
                return DoorState.CLOSED;

            default:
                return DoorState.OPEN;
        }
    }

    private DoorState getGranularState() {
        Item stateItem = getItemRegistry().get(getDoorStateItemName());
        StringType stringState = (StringType) stateItem.getStateAs(StringType.class);
        if (stringState == null) {
            logger.warn("Could not get StringType from {}", getDoorStateItemName());
            return DoorState.OPEN;
        } else {
            return OpenHabDoorState.fromString(stringState.toString()).toHomekitState();
        }
    }

}