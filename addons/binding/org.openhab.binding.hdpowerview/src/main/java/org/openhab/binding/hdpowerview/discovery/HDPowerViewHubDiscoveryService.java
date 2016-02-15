package org.openhab.binding.hdpowerview.discovery;

import java.net.UnknownHostException;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hdpowerview.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.config.HDPowerViewHubConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.netbios.NbtAddress;

public class HDPowerViewHubDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubDiscoveryService.class);

    public HDPowerViewHubDiscoveryService() {
        super(HDPowerViewBindingConstants.SUPPORTED_THING_TYPES_UIDS, 600, true);
    }

    @Override
    protected void startScan() {
        try {
            NbtAddress address = NbtAddress.getByName(HDPowerViewBindingConstants.NETBIOS_NAME);
            if (address != null) {
                String ip = address.getInetAddress().getHostAddress();
                ThingUID thingUID = new ThingUID(HDPowerViewBindingConstants.THING_TYPE_HUB, ip.replace('.', '_'));
                DiscoveryResult result = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.IP_ADDRESS, ip)
                        .withLabel("PowerView Hub (" + ip + ")").build();
                thingDiscovered(result);
                stopScan();
            } else {
                stopScan();
            }
        } catch (UnknownHostException e) {
            stopScan();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

}
