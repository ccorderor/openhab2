package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import jcifs.util.Base64;

/**
 * A list of Scenes, as returned by the HD Power View Hub
 *
 * @author Andy Lintner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenes {

    public List<Scene> sceneData;
    public List<String> sceneIds;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scene {
        public int id;
        @JsonSerialize(include = Inclusion.ALWAYS)
        String name;
        public int roomId;
        public int order;
        public int colorId;
        public int iconId;

        public String getName() {
            return new String(Base64.decode(name));
        }
    }

}
