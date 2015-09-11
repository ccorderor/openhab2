package org.openhab.io.homekit.internal.accessories;

import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitAccessory;

public interface GroupedAccessory extends HomekitAccessory {

    public String getGroupName();

    public void addCharacteristic(HomekitTaggedItem item);

    public boolean isComplete();
}
