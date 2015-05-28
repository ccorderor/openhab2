package com.beowulfe.openhab.homekit.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.events.AbstractEventSubscriber;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.openhab.homekit.HomekitAccessoryUpdater;

public class HomekitAccessoryUpdaterImpl extends AbstractEventSubscriber implements HomekitAccessoryUpdater {

	private final Map<String, Set<Subscription>> subscriptionsByName = new HashMap<>();
	
	@Override
	public void receiveUpdate(String itemName, State newState) {
		processEvent(itemName, newState);
	}
	
	@Override
	public void receiveCommand(String itemName, Command command) {
		processEvent(itemName, command);
	}

	@Override
	public synchronized void subscribe(String itemName, Class<? extends State> type,
			HomekitCharacteristicChangeCallback callback) {
		if (!subscriptionsByName.containsKey(itemName)) {
			subscriptionsByName.put(itemName, new HashSet<>());
		}
		subscriptionsByName.get(itemName).add(new Subscription(type, callback));
	}

	@Override
	public void unsubscribe(String itemName, Class<? extends State> type) {
		Set<Subscription> subscriptions = subscriptionsByName.get(itemName);
		if (subscriptions != null) {
			Iterator<Subscription> i = subscriptions.iterator();
			while(i.hasNext()) {
				if (i.next().type == type) {
					i.remove();
				}
			}
		}
	}
	
	private void processEvent(String itemName, Type newState) {
		Set<Subscription> subscriptions = subscriptionsByName.get(itemName);
		if (subscriptions != null) {
			for (Subscription subscription: subscriptions) {
				if (subscription.type.isAssignableFrom(newState.getClass())) {
					subscription.callback.changed();
				}
			}
		}
	}
	
	private static class Subscription {
		public final Class<? extends State> type;
		public final HomekitCharacteristicChangeCallback callback;
		
		public Subscription(Class<? extends State> type,
				HomekitCharacteristicChangeCallback callback) {
			this.type = type;
			this.callback = callback;
		}
	}
}
