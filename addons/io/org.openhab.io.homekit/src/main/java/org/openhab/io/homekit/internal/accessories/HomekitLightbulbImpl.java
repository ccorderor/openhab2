package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.homekit.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.Lightbulb;

public class HomekitLightbulbImpl implements Lightbulb {

	private final int accessoryId;
	private final String itemName;
	private final ItemRegistry itemRegistry;
	private final HomekitAccessoryUpdater updater;
	private Logger logger = LoggerFactory
			.getLogger(HomekitLightbulbImpl.class);
	
	public HomekitLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry, HomekitAccessoryUpdater updater) {
		this.accessoryId = taggedItem.getId();
		this.itemName = taggedItem.getItem().getName();
		this.itemRegistry = itemRegistry;
		this.updater = updater;
		if (taggedItem.getItem().getStateAs(OnOffType.class) == null) {
			logger.error("Type "+taggedItem.getItem().getName()+" does not support OnOff");
		}
	}

	@Override
	public int getId() {
		return accessoryId;
	}

	@Override
	public String getLabel() {
		return itemName;
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
		//TODO: Figure out how to flash the light without timer overhead
	}

	@Override
	public CompletableFuture<Boolean> getLightbulbPowerState() {
		Item item = itemRegistry.get(itemName);
		OnOffType state = (OnOffType) item.getStateAs(OnOffType.class);
		return CompletableFuture.completedFuture(state == OnOffType.ON);
	}

	@Override
	public CompletableFuture<Void> setLightbulbPowerState(boolean value)
			throws Exception {
		Item item = itemRegistry.get(itemName);
		if (item instanceof GenericItem) {
			((GenericItem) item).setState(value ? OnOffType.ON : OnOffType.OFF);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void subscribeLightbulbPowerState(
			HomekitCharacteristicChangeCallback callback) {
		updater.subscribe(itemName, OnOffType.class, callback);
	}

	@Override
	public void unsubscribeLightbulbPowerState() {
		updater.unsubscribe(itemName, OnOffType.class);
	}

}
