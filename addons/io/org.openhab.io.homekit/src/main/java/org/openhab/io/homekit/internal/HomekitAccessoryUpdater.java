package org.openhab.io.homekit.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

public class HomekitAccessoryUpdater {

    private Logger logger = LoggerFactory.getLogger(HomekitAccessoryUpdater.class);
    private final ConcurrentMap<String, Subscription> subscriptionsByName = new ConcurrentHashMap<>();

    public void subscribe(GenericItem item, HomekitCharacteristicChangeCallback callback) {
        if (item == null) {
            return;
        }
        if (subscriptionsByName.containsKey(item.getName())) {
            logger.error("Received duplicate subscription on " + item.getName());
        }
        subscriptionsByName.compute(item.getName(), (k, v) -> {
            if (v != null) {
                logger.error("Received duplicate subscription on " + item.getName());
                unsubscribe(item);
            }
            Subscription subscription = (changedItem, oldState, newState) -> callback.changed();
            item.addStateChangeListener(subscription);
            return subscription;
        });
    }

    public void unsubscribe(GenericItem item) {
        if (item == null) {
            return;
        }
        subscriptionsByName.computeIfPresent(item.getName(), (k, v) -> {
            item.removeStateChangeListener(v);
            return null;
        });
    }

    @FunctionalInterface
    private static interface Subscription extends StateChangeListener {

        @Override
        public abstract void stateChanged(Item item, State oldState, State newState);

        @Override
        default public void stateUpdated(Item item, State state) {
            // Do nothing on non-change update
        }
    }

}
