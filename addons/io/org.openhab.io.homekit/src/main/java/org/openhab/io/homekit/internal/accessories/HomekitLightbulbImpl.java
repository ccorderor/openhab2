package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Lightbulb;

public class HomekitLightbulbImpl extends AbstractHomekitAccessoryImpl implements Lightbulb {

    private Logger logger = LoggerFactory.getLogger(HomekitLightbulbImpl.class);

    public HomekitLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater);
        if (taggedItem.getItem().getStateAs(OnOffType.class) == null) {
            logger.error("Type " + taggedItem.getItem().getName() + " does not support OnOff");
        }
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        Item item = getItemRegistry().get(getItemName());
        OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean value) throws Exception {
        Item item = getItemRegistry().get(getItemName());
        if (item instanceof GenericItem) {
            ((GenericItem) item).setState(value ? OnOffType.ON : OnOffType.OFF);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe((GenericItem) getItemRegistry().get(getItemName()), callback);
    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        getUpdater().unsubscribe((GenericItem) getItemRegistry().get(getItemName()));
    }

}
