package org.openhab.binding.hdpowerview.internal.api.requests;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

@JsonSerialize
public class ShadeMove {

    @JsonSerialize(include = Inclusion.ALWAYS)
    ShadeIdPosition shade;

    public ShadeMove(int id, ShadePosition position) {
        this.shade = new ShadeIdPosition(id, position);
    }
}
