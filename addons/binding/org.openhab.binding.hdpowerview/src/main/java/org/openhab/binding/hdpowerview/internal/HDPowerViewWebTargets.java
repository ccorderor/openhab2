package org.openhab.binding.hdpowerview.internal;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.requests.ShadeMove;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDPowerViewWebTargets {

    private WebTarget base;
    private WebTarget shades;
    private WebTarget shadeMove;
    private WebTarget sceneActivate;
    private WebTarget scenes;
    private Logger logger = LoggerFactory.getLogger(HDPowerViewWebTargets.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public HDPowerViewWebTargets(Client client, String ipAddress) {
        base = client.target("http://" + ipAddress + "/api");
        shades = base.path("shades/");
        shadeMove = base.path("shades/{id}");
        sceneActivate = base.path("scenes");
        scenes = base.path("scenes/");
    }

    public Shades getShades() throws JsonParseException, JsonMappingException, IOException {
        Response response = invoke(shades.request().buildGet(), shades);
        if (response != null) {
            String result = response.readEntity(String.class);
            return mapper.readValue(result, Shades.class);
        } else {
            return null;
        }
    }

    public Response moveShade(int shadeId, ShadePosition position)
            throws JsonGenerationException, JsonMappingException, IOException {
        WebTarget target = shadeMove.resolveTemplate("id", shadeId);
        String body = mapper.writeValueAsString(new ShadeMove(shadeId, position));
        return invoke(target.request().buildPut(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE)), shadeMove);
    }

    public Scenes getScenes() throws JsonParseException, JsonMappingException, IOException {
        Response response = invoke(scenes.request().buildGet(), scenes);
        if (response != null) {
            String result = response.readEntity(String.class);
            return mapper.readValue(result, Scenes.class);
        } else {
            return null;
        }
    }

    public void activateScene(int sceneId) {
        WebTarget target = sceneActivate.queryParam("sceneid", sceneId);
        invoke(target.request().buildGet(), sceneActivate);
    }

    private Response invoke(Invocation invocation, WebTarget target) {
        Response response;
        synchronized (this) {
            response = invocation.invoke();
        }

        if (response.getStatus() != 200) {
            logger.error("Bridge returned " + response.getStatus() + " while invoking " + target.getUri() + " : "
                    + response.readEntity(String.class));
            return null;
        } else if (!response.hasEntity()) {
            logger.error("Bridge returned null response" + " while invoking " + target.getUri());
            return null;
        }

        return response;
    }

}
