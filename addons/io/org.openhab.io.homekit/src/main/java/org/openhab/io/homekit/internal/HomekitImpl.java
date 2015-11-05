package org.openhab.io.homekit.internal;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Dictionary;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.homekit.Homekit;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;

/**
 * The managed service for providing access to OpenHAB items via the Homekit API
 *
 * @author Andy Lintner
 */
public class HomekitImpl implements ManagedService, Homekit {

    private HomekitSettings settings;
    private HomekitServer homekit;
    private HomekitRoot bridge;
    private StorageService storageService;
    private final HomekitChangeListener homekitRegistry = new HomekitChangeListener();
    private Logger logger = LoggerFactory.getLogger(HomekitImpl.class);
    private ItemRegistry itemRegistry;

    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @Activate
    protected synchronized void activate(ComponentContext componentContext) {

    }

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
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        HomekitSettings newSettings;
        try {
            newSettings = HomekitSettings.create(properties);
        } catch (UnknownHostException e) {
            throw new ConfigurationException("networkInterface", e.getMessage(), e);
        }
        homekitRegistry.setSettings(newSettings);
        if (settings != null) {
            if (!settings.equals(newSettings)) {
                deactivate();
            }
        }
        settings = newSettings;
        if (homekit == null) {
            try {
                homekitRegistry.setItemRegistry(itemRegistry);
                start();
            } catch (Exception e) {
                logger.error("Could not initialize homekit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void refreshAuthInfo() throws IOException {
        if (bridge != null) {
            bridge.refreshAuthInfo();
        }
    }

    @Override
    public void allowUnauthenticatedRequests(boolean allow) {
        if (bridge != null) {
            bridge.allowUnauthenticatedRequests(allow);
        }
    }

    private void start() throws IOException, InvalidAlgorithmParameterException {
        homekit = new HomekitServer(settings.getNetworkInterface(), settings.getPort());
        bridge = homekit.createBridge(new HomekitAuthInfoImpl(storageService, settings.getPin()), settings.getName(),
                settings.getManufacturer(), settings.getModel(), settings.getSerialNumber());
        bridge.start();
        homekitRegistry.setBridge(bridge);
    }
}
