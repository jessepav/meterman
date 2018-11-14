package com.illcode.meterman;

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

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static final int VISITED = 0;

}
