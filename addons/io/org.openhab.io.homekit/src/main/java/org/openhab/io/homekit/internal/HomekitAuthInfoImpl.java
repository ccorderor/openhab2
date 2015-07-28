package org.openhab.io.homekit.internal;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.Base64;

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;

public class HomekitAuthInfoImpl implements HomekitAuthInfo {

	private final Storage<String> storage;
	private final String mac;
	private final BigInteger salt;
	private final byte[] privateKey;
	private final String pin;
	
	public HomekitAuthInfoImpl(StorageService storageService, String pin) throws InvalidAlgorithmParameterException {
		storage = storageService.getStorage("homekit");
		initializeStorage();
		this.pin = pin;
		mac = storage.get("mac");
		salt = new BigInteger(storage.get("salt"));
		privateKey = Base64.getDecoder().decode(storage.get("privateKey"));
	}
	
	@Override
	public void createUser(String username, byte[] publicKey) {
		storage.put(createUserKey(username), Base64.getEncoder().encodeToString(publicKey));
	}

	@Override
	public String getMac() {
		return mac;
	}

	@Override
	public String getPin() {
		return pin;
	}

	@Override
	public byte[] getPrivateKey() {
		return privateKey;
	}

	@Override
	public BigInteger getSalt() {
		return salt;
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		String encodedKey = storage.get(createUserKey(username));
		if (encodedKey != null) {
			return Base64.getDecoder().decode(encodedKey);
		} else {
			return null;
		}
	}

	@Override
	public void removeUser(String username) {
		storage.remove(createUserKey(username));
	}
	
	private String createUserKey(String username) {
		return "user_"+username;
	}
	
	private void initializeStorage() throws InvalidAlgorithmParameterException {
		if (storage.get("mac") == null) {
			storage.put("mac", HomekitServer.generateMac());
		}
		if (storage.get("salt") == null) {
			storage.put("salt", HomekitServer.generateSalt().toString());
		}
		if (storage.get("privateKey") == null) {
			storage.put("privateKey", Base64.getEncoder().encodeToString(HomekitServer.generateKey()));
		}
	}

}
