package com.illcode.meterman;

import com.illcode.meterman.impl.DarkRoom;

import java.util.LinkedList;
import java.util.List;

/**
 * The attribute constants used with the Entity and Room attribute methods
 */
public final class Attributes
{
    /**
     * The number of attributes reserved by the game engine. Thus, any game-specific
     * attributes should start at {@code RESERVED_ATTRIBUTES}.
     */
    public static final int RESERVED_ATTRIBUTES = 32;

    //
    // Entity attributes
    //

    /** If set, the entity will not be displayed in the room entities list. */
    public static final int CONCEALED = 0;

    /** If set, the entity can be taken (i.e. transferred to the player inventory) and dropped */
    public static final int TAKEABLE = 1;

    /** The entity is wearable */
    public static final int WEARABLE = 2;

    /** The entity is equippable */
    public static final int EQUIPPABLE = 3;

    /** This entity is a light source. */
    public static final int LIGHTSOURCE = 4;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static final int VISITED = 0;

    /** This room is naturally dark.
     *  @see DarkRoom */
    public static final int DARK = 1;

}
