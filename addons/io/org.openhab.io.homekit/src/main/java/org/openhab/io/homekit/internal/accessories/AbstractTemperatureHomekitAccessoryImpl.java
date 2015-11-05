package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

abstract class AbstractTemperatureHomekitAccessoryImpl<T extends GenericItem> extends AbstractHomekitAccessoryImpl<T> {

    private final HomekitSettings settings;

    public AbstractTemperatureHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, HomekitSettings settings, Class<T> expectedItemClass) {
        super(taggedItem, itemRegistry, updater, expectedItemClass);
        this.settings = settings;
    }

    public double getMaximumTemperature() {
        return settings.getMaximumTemperature();
    }

    public double getMinimumTemperature() {
        return settings.getMinimumTemperature();
    }

    protected double convertToCelsius(double degrees) {
        if (settings.useFahrenheitTemperature()) {
            return Math.round((5d / 9d) * (degrees - 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }

    protected double convertFromCelsius(double degrees) {
        if (settings.useFahrenheitTemperature()) {
            return Math.round((((9d / 5d) * degrees) + 32d) * 1000d) / 1000d;
        } else {
            return degrees;
        }
    }
}
