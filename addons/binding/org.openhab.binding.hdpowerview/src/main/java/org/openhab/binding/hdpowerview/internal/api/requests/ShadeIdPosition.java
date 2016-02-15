package org.openhab.binding.hdpowerview.internal.api.requests;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * The position of a shade to set
 *
 * @author Andy Lintner
 */
@JsonSerialize
class ShadeIdPosition {

    @JsonSerialize(include = Inclusion.ALWAYS)
    int id;

    @JsonSerialize(include = Inclusion.ALWAYS)
    ShadePosition positions;

    public ShadeIdPosition(int id, ShadePosition position) {
        this.id = id;
        this.positions = position;
    }
}
