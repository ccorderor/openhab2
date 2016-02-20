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
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.VerticalTiltingWindowCovering;

/**
 * openHAB implementation of a HomeKit WindowCovering capable of vertical tilt
 * 
 * @author Andy Lintner
 */
public class HomekitVerticalTiltingWindowCoveringImpl extends HomekitWindowCoveringImpl
        implements VerticalTiltingWindowCovering {

    private String verticalTiltAngleItemName;

    public HomekitVerticalTiltingWindowCoveringImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater);
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        if (item.getCharacteristicType() == HomekitCharacteristicType.VERTICAL_TILT_ANGLE) {
            verticalTiltAngleItemName = item.getItem().getName();
        } else {
            super.addCharacteristic(item);
        }
    }

    @Override
    public boolean isComplete() {
        return verticalTiltAngleItemName != null && super.isComplete();
    }

    @Override
    public CompletableFuture<Integer> getCurrentVerticalTiltAngle() {
        GenericItem item = getGenericItem(verticalTiltAngleItemName);
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(90);
        }
        return CompletableFuture.completedFuture((int) Math.round(90d * state.intValue() / 100d));
    }

    @Override
    public CompletableFuture<Integer> getTargetVerticalTiltAngle() {
        return getCurrentVerticalTiltAngle();
    }

    @Override
    public CompletableFuture<Void> setTargetVerticalTiltAngle(int angle) throws Exception {
        DimmerItem item = getGenericItem(verticalTiltAngleItemName);
        item.send(new PercentType((int) Math.max(0, Math.round(angle / 90d * 100d))));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentVerticalTiltAngle(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(verticalTiltAngleItemName), "currentVerticalTiltAngle", callback);
    }

    @Override
    public void subscribeTargetVerticalTiltAngle(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(verticalTiltAngleItemName), "targetVerticalTiltAngle", callback);
    }

    @Override
    public void unsubscribeCurrentVerticalTiltAngle() {
        getUpdater().unsubscribe(getGenericItem(verticalTiltAngleItemName), "currentVerticalTiltAngle");
    }

    @Override
    public void unsubscribeTargetVerticalTiltAngle() {
        getUpdater().unsubscribe(getGenericItem(verticalTiltAngleItemName), "targetVerticalTiltAngle");
    }

    @Override
    public boolean allowNegativeVerticalTiltAngle() {
        return false;
    }

}
