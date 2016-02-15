package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

import jcifs.util.Base64;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Shades {

    public List<Shade> shadeData;
    public List<String> shadeIds;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Shade {
        public int id;
        @JsonSerialize(include = Inclusion.ALWAYS)
        String name;
        public int roomId;
        public int groupId;
        public int order;
        public int type;
        public double batteryStrength;
        public int batterStatus;
        public boolean batteryIsLow;
        public ShadePosition positions;

        public String getName() {
            return new String(Base64.decode(name));
        }
    }
}
