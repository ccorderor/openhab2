package org.openhab.io.homekit.internal;

import org.eclipse.smarthome.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomekitTaggedItem {

	private Integer homekitId;
	private HomekitDeviceType homekitType;
	private final Item item;
	private Logger logger = LoggerFactory
			.getLogger(HomekitTaggedItem.class);
	
	public HomekitTaggedItem(Item item) {
		this.item = item;
		for (String tag: item.getTags()) {
			if (tag.startsWith("homekitId:")) {
				homekitId = Integer.valueOf(tag.substring("homekitId:".length()));
			} else if (tag.startsWith("homekitType:")) {
				String typeString = tag.substring("homekitType:".length());
				homekitType = HomekitDeviceType.valueOfCaseInsensitive(typeString);
				if (homekitType == null) {
					logger.error("Unrecognized homekit type: "+typeString);
				}
			}
		}
		if ((homekitId != null && homekitType == null) || (homekitType != null && homekitId == null)) {
			logger.error("homekitId and homekitType must both be specified as tags for item " +
					item.getName());
		}
	}
	
	public boolean isTagged() {
		return homekitId != null && homekitType != null;
	}
	
	public Integer getId() {
		//1 is reserved for the bridge itself, and two-based array sequences would be strange
		return homekitId + 1;
	}
	
	public HomekitDeviceType getType() {
		return homekitType;
	}
	
	public Item getItem() {
		return item;
	}
}
