package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Switch;

/**
 * Implements Switch using an Item that provides an On/Off state.
 *
 * @author Andy Lintner
 */
public class HomekitSwitchImpl extends AbstractHomekitAccessoryImpl implements Switch {

    private Logger logger = LoggerFactory.getLogger(HomekitLightbulbImpl.class);

    public HomekitSwitchImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater);
        if (taggedItem.getItem().getStateAs(OnOffType.class) == null) {
            logger.error("Type " + taggedItem.getItem().getName() + " does not support OnOff");
        }
    }

    @Override
    public CompletableFuture<Boolean> getSwitchState() {
        OnOffType state = (OnOffType) getItem().getStateAs(OnOffType.class);
        return CompletableFuture.completedFuture(state == OnOffType.ON);
    }

    @Override
    public CompletableFuture<Void> setSwitchState(boolean state) throws Exception {
        getItem().setState(state ? OnOffType.ON : OnOffType.OFF);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeSwitchState(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), callback);
    }

    @Override
    public void unsubscribeSwitchState() {
        getUpdater().unsubscribe(getItem());
    }

}
