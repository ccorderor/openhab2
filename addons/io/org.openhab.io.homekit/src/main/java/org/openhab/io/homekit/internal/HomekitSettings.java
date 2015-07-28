package org.openhab.io.homekit.internal;

import java.util.Dictionary;

import org.osgi.framework.FrameworkUtil;

public class HomekitSettings {

	private final static String NAME = "OpenHAB Homekit Bridge";
	private final static String MANUFACTURER = "OpenHAB";
	private final static String SERIAL_NUMBER = "none";
	
	private int port;
	private String pin;
	
	public static HomekitSettings create(Dictionary<String, ?> properties) {
		HomekitSettings settings = new HomekitSettings();
		String portString = (String) properties.get("port");
		if (portString == null) {
			throw new RuntimeException("No homekit port found");
		}
		settings.port = Integer.parseInt(portString);
		settings.pin = (String) properties.get("pin");
		if (settings.pin == null) {
			throw new RuntimeException("No homekit pin found");
		}
		return settings;
	}
	
	public String getName() {
		return NAME;
	}
	
	public String getManufacturer() {
		return MANUFACTURER;
	}
	
	public String getSerialNumber() {
		return SERIAL_NUMBER;
	}
	
	public String getModel() {
		return FrameworkUtil.getBundle(getClass()).getVersion().toString();
	}
	
	public int getPort() {
		return port;
	}
	
	public String getPin() {
		return pin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pin == null) ? 0 : pin.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HomekitSettings other = (HomekitSettings) obj;
		if (pin == null) {
			if (other.pin != null)
				return false;
		} else if (!pin.equals(other.pin))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}
