package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.DimmableLightbulb;

/**
 * Implements DimmableLightBulb using an Item that provides a On/Off and Percent state.
 *
 * @author Andy Lintner
 */
class HomekitDimmableLightbulbImpl extends HomekitLightbulbImpl implements DimmableLightbulb {

    private Logger logger = LoggerFactory.getLogger(HomekitDimmableLightbulbImpl.class);

    public HomekitDimmableLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater);
        if (taggedItem.getItem().getStateAs(PercentType.class) == null) {
            logger.error("Type " + taggedItem.getItem().getName() + " does not support Percent");
        }
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        PercentType state = (PercentType) getItem().getStateAs(PercentType.class);
        return CompletableFuture.completedFuture(state.intValue());
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        getItem().setState(new PercentType(value));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeBrightness(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "brightness", callback);
    }

    @Override
    public void unsubscribeBrightness() {
        getUpdater().unsubscribe(getItem(), "brightness");
    }

}
