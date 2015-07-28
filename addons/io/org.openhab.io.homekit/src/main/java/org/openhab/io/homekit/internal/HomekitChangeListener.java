package org.openhab.io.homekit.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.openhab.io.homekit.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.accessories.HomekitLightbulbImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitRoot;

public class HomekitChangeListener implements ItemRegistryChangeListener {

	private HomekitRoot bridge;
	private final List<HomekitAccessory> createdAccessories = new LinkedList<>();
	private final Set<Integer> createdIds = new HashSet<>();
	private ItemRegistry itemRegistry;
	private HomekitAccessoryUpdater updater;
	private Logger logger = LoggerFactory
			.getLogger(HomekitChangeListener.class);
	
	@Override
	public synchronized void added(Item item) {
		HomekitTaggedItem taggedItem = new HomekitTaggedItem(item);
		if (taggedItem.isTagged() && !createdIds.contains(taggedItem.getId())) {
			HomekitAccessory accessory;
			switch(taggedItem.getType()) {
			case LIGHTBULB:
				accessory = new HomekitLightbulbImpl(taggedItem, itemRegistry, updater);;
				break;
				
			default:
				logger.error("Unknown homekit type: "+taggedItem.getType());
				return;
			}
			if (bridge != null) {
				bridge.addAccessory(accessory);
			}
			createdAccessories.add(accessory);
			createdIds.add(accessory.getId());
		}
	}

	@Override
	public void allItemsChanged(Collection<String> oldItemNames) {
		clearAccessories();
	}

	@Override
	public synchronized void removed(Item item) {
		HomekitTaggedItem taggedItem = new HomekitTaggedItem(item);
		if (taggedItem.isTagged()) {
			Iterator<HomekitAccessory> i = createdAccessories.iterator();
			while(i.hasNext()) {
				HomekitAccessory accessory = i.next();
				if (accessory.getId() == taggedItem.getId()) {
					bridge.removeAccessory(accessory);
					i.remove();
				}
			}
		}
	}

	@Override
	public void updated(Item oldElement, Item element) {
		removed(oldElement);
		added(element);
	}
	
	public synchronized void clearAccessories() {
		while(!createdAccessories.isEmpty()) {
			bridge.removeAccessory(createdAccessories.remove(0));
		}
		createdIds.clear();
	}
	
	public synchronized void setBridge(HomekitRoot bridge) {
		this.bridge = bridge;
		createdAccessories.forEach(accessory -> bridge.addAccessory(accessory));
	}

	public synchronized void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		itemRegistry.addRegistryChangeListener(this);
		itemRegistry.getAll().forEach(item -> added(item));
	}
	
	public void setUpdater(HomekitAccessoryUpdater updater) {
		this.updater = updater;
	}

}
