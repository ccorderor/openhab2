package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

/**
 * Abstract class for HomekitAccessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitAccessoryImpl implements HomekitAccessory {

    private final int accessoryId;
    private final String itemName;
    private final String itemLabel;
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryUpdater updater;

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        this.accessoryId = taggedItem.getId();
        this.itemName = taggedItem.getItem().getName();
        this.itemLabel = taggedItem.getItem().getLabel();
        this.itemRegistry = itemRegistry;
        this.updater = updater;
    }

    @Override
    public int getId() {
        return accessoryId;
    }

    @Override
    public String getLabel() {
        return itemLabel;
    }

    @Override
    public String getManufacturer() {
        return "none";
    }

    @Override
    public String getModel() {
        return "none";
    }

    @Override
    public String getSerialNumber() {
        return "none";
    }

    @Override
    public void identify() {
        // We're not going to support this for now
    }

    protected ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    protected String getItemName() {
        return itemName;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    protected GenericItem getItem() {
        return (GenericItem) getItemRegistry().get(getItemName());
    }
}
