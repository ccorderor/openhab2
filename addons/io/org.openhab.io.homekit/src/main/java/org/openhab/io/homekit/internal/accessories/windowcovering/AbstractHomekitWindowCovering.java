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

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.openhab.io.homekit.internal.accessories.AbstractHomekitAccessoryImpl;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.WindowCovering;
import com.beowulfe.hap.accessories.properties.WindowCoveringPositionState;

/**
 * Abstract implementation of a HomeKit WindowCovering accessory
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitWindowCovering<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T>
        implements WindowCovering {

    public AbstractHomekitWindowCovering(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
    }

    @Override
    public CompletableFuture<Integer> getCurrentPosition() {
        RollershutterItem item = getRollerShutter();
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(100 - state.intValue());
    }

    @Override
    public CompletableFuture<WindowCoveringPositionState> getPositionState() {
        return CompletableFuture.completedFuture(WindowCoveringPositionState.STOPPED);
    }

    @Override
    public CompletableFuture<Integer> getTargetPosition() {
        return getCurrentPosition();
    }

    @Override
    public CompletableFuture<Void> setHoldPosition(boolean doStop) throws Exception {
        RollershutterItem item = getRollerShutter();
        item.send(doStop ? StopMoveType.STOP : StopMoveType.MOVE);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setTargetPosition(int target) throws Exception {
        RollershutterItem item = getRollerShutter();
        item.send(new PercentType(100 - target));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getRollerShutter(), "position", callback);
    }

    @Override
    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        // No implementation
    }

    @Override
    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getRollerShutter(), "targetPosition", callback);
    }

    @Override
    public void unsubscribeCurrentPosition() {
        getUpdater().unsubscribe(getRollerShutter(), "position");
    }

    @Override
    public void unsubscribePositionState() {
        // No implementation
    }

    @Override
    public void unsubscribeTargetPosition() {
        getUpdater().unsubscribe(getRollerShutter(), "targetPosition");
    }

    protected abstract String getPositionItemName();

    private RollershutterItem getRollerShutter() {
        Item item = getItemRegistry().get(getPositionItemName());
        if (!(item instanceof RollershutterItem)) {
            throw new RuntimeException("Expected " + getPositionItemName() + " to be RollerShutterItem, found "
                    + item.getClass().getCanonicalName());
        }
        return (RollershutterItem) item;
    }

}