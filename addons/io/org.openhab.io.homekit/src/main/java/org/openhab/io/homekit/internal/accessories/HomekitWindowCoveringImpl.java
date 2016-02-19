package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.WindowCovering;
import com.beowulfe.hap.accessories.properties.WindowCoveringPositionState;

public class HomekitWindowCoveringImpl extends AbstractHomekitAccessoryImpl<GroupItem>
        implements WindowCovering, GroupedAccessory {

    private final String groupName;
    private final Logger logger = LoggerFactory.getLogger(HomekitWindowCoveringImpl.class);

    private String positionItemName;
    private String obstructionDetectedItemName; // Optional

    public HomekitWindowCoveringImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
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
    public CompletableFuture<Integer> getCurrentPosition() {
        RollershutterItem item = getRollerShutter();
        PercentType state = (PercentType) item.getStateAs(PercentType.class);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(100 - state.intValue());
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
        item.setState(new PercentType(100 - target));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getRollerShutter(), "position", callback);
    }

    @Override
    public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
        if (obstructionDetectedItemName != null) {
            getUpdater().subscribe(getGenericItem(obstructionDetectedItemName), callback);
        }
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
    public void unsubscribeObstructionDetected() {
        if (obstructionDetectedItemName != null) {
            getUpdater().unsubscribe(getGenericItem(obstructionDetectedItemName));
        }
    }

    @Override
    public void unsubscribePositionState() {
        // No implementation
    }

    @Override
    public void unsubscribeTargetPosition() {
        getUpdater().unsubscribe(getRollerShutter(), "targetPosition");
    }

    private RollershutterItem getRollerShutter() {
        Item item = getItemRegistry().get(positionItemName);
        if (!(item instanceof RollershutterItem)) {
            throw new RuntimeException("Expected " + positionItemName + " to be RollerShutterItem, found "
                    + item.getClass().getCanonicalName());
        }
        return (RollershutterItem) item;
    }

}
