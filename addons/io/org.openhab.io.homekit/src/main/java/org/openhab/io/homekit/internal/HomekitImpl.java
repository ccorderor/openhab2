package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Dictionary;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.HomekitAccessoryUpdater;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

public class HomekitImpl implements ManagedService {

	private HomekitSettings settings;
	private HomekitServer homekit;
	private HomekitRoot bridge;
	private StorageService storageService;
	private final HomekitChangeListener homekitRegistry = new HomekitChangeListener();
	private Logger logger = LoggerFactory
			.getLogger(HomekitImpl.class);
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	public void setUpdater(HomekitAccessoryUpdater updater) {
		homekitRegistry.setUpdater(updater);
	}
	
	public void setItemRegistry(ItemRegistry itemRegistry) {
		homekitRegistry.setItemRegistry(itemRegistry);
	}
    
	@Activate
	protected synchronized void activate(ComponentContext componentContext) { }
	
	@Deactivate
	protected void deactivate() {
		homekitRegistry.clearAccessories();
		bridge.stop();
		homekit.stop();
		bridge = null;
		homekit = null;
		homekitRegistry.setBridge(null);
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		HomekitSettings newSettings = HomekitSettings.create(properties);
		if (settings != null) {
			if (!settings.equals(newSettings)) {
				deactivate();
			}
		}
		settings = newSettings;
		if (homekit == null) {
			try {
				start();
			} catch (Exception e) {
				logger.error("Could not initialize homekit: "+e.getMessage(), e);
			}
		}
	}
	
	private void start()  throws IOException, InvalidAlgorithmParameterException {
		homekit = new HomekitServer(settings.getPort());
		bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
				settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
		bridge.start();
		homekitRegistry.setBridge(bridge);
	}
}
