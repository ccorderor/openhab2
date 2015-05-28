package com.beowulfe.openhab.homekit.internal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.model.item.BindingConfigParseException;
import org.eclipse.smarthome.model.item.BindingConfigReader;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

import com.beowulfe.hap.HomekitAccessory;
import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;
import com.beowulfe.openhab.homekit.HomekitAccessoryUpdater;
import com.beowulfe.openhab.homekit.internal.accessories.HomekitLightbulbImpl;

public class HomekitImpl implements BindingConfigReader {

	private static String BINDING_TYPE = "homekit";
	private HomekitSettings settings;
	private HomekitServer homekit;
	private HomekitRoot bridge;
	private StorageService storageService;
	private final List<HomekitAccessory> createdAccessories = new LinkedList<>();
	private ItemRegistry itemRegistry;
	private HomekitAccessoryUpdater updater;
	
	@Override
	public String getBindingType() {
		return BINDING_TYPE;
	}

	@Override
	public void validateItemType(String itemType, String bindingConfig)
			throws BindingConfigParseException {
		if (!HomekitDeviceType.supportsOpenhabType(itemType)) {
			throw new BindingConfigParseException(itemType + " is not a supported type for Homekit");
		}
	}

	@Override
	public synchronized void processBindingConfiguration(String context, String itemType,
			String itemName, String bindingConfig)
			throws BindingConfigParseException {
		int accessoryId = Integer.parseInt(bindingConfig) + 1;
		HomekitAccessory accessory = new HomekitLightbulbImpl(accessoryId, itemName, itemRegistry, updater);
		bridge.addAccessory(accessory);
		createdAccessories.add(accessory);
	}

	@Override
	public void startConfigurationUpdate(String context) {
		clearAccessories();
	}

	@Override
	public void stopConfigurationUpdate(String context) {
		
	}
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	public void setUpdater(HomekitAccessoryUpdater updater) {
		this.updater = updater;
	}
	
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}
    
	@Activate
	protected synchronized void activate(ComponentContext componentContext) throws IOException, InvalidAlgorithmParameterException {
		Dictionary<String, Object> properties = componentContext.getProperties();
		settings = HomekitSettings.create(properties);
		homekit = new HomekitServer(settings.getPort());
		bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
				settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
		bridge.start();
	}
	
	@Deactivate
	protected void deactivate() {
		clearAccessories();
		bridge.stop();
		homekit.stop();
		bridge = null;
		homekit = null;
	}
	
	private void clearAccessories() {
		while(!createdAccessories.isEmpty()) {
			bridge.removeAccessory(createdAccessories.remove(0));
		}
	}

}
