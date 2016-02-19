package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.HorizontalTiltingWindowCovering;

public class HomekitHorizontalTiltingWindowCoveringImpl extends HomekitWindowCoveringImpl
        implements HorizontalTiltingWindowCovering {

    private String horizontalTiltAngleItemName;

    public HomekitHorizontalTiltingWindowCoveringImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater);
    }

    @Override
    public void addCharacteristic(HomekitTaggedItem item) {
        if (item.getCharacteristicType() == HomekitCharacteristicType.HORIZONTAL_TILT_ANGLE) {
            horizontalTiltAngleItemName = item.getItem().getName();
        } else {
            super.addCharacteristic(item);
        }
    }

    @Override
    public boolean isComplete() {
        return horizontalTiltAngleItemName != null && super.isComplete();
    }

    @Override
    public CompletableFuture<Integer> getCurrentHorizontalTiltAngle() {
        GenericItem item = getGenericItem(horizontalTiltAngleItemName);
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(90);
        }
        return CompletableFuture.completedFuture((int) Math.round(90d * state.intValue() / 100d));
    }

    @Override
    public CompletableFuture<Integer> getTargetHorizontalTiltAngle() {
        return getCurrentHorizontalTiltAngle();
    }

    @Override
    public CompletableFuture<Void> setTargetHorizontalTiltAngle(int angle) throws Exception {
        GenericItem item = getGenericItem(horizontalTiltAngleItemName);
        item.setState(new PercentType((int) Math.max(0, Math.round(angle / 90d * 100d))));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentHorizontalTiltAngle(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(horizontalTiltAngleItemName), "currentHorizontalTiltAngle", callback);
    }

    @Override
    public void subscribeTargetHorizontalTiltAngle(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getGenericItem(horizontalTiltAngleItemName), "targetHorizontalTiltAngle", callback);
    }

    @Override
    public void unsubscribeCurrentHorizontalTiltAngle() {
        getUpdater().unsubscribe(getGenericItem(horizontalTiltAngleItemName), "currentHorizontalTiltAngle");
    }

    @Override
    public void unsubscribeTargetHorizontalTiltAngle() {
        getUpdater().unsubscribe(getGenericItem(horizontalTiltAngleItemName), "targetHorizontalTiltAngle");
    }

    @Override
    public boolean allowNegativeHorizontalTiltAngle() {
        return false;
    }

}
