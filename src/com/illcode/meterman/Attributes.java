package com.illcode.meterman;

/**
 * The attribute constants used with the Entity and Room attribute methods
 */
public final class Attributes
{
    //
    // Entity attributes
    //

    /** If set, the entity will not be displayed in the room entities list. */
    public static final int CONCEALED = 0;

    /** If set, the entity can be taken (i.e. transferred to the player inventory) and dropped */
    public static final int TAKEABLE = 1;

    /** The entity represents a door */
    public static final int DOOR = 2;

    /** The entity represents a key */
    public static final int KEY = 3;

    /** The entity is wearable */
    public static final int WEARABLE = 4;

    /** The entity is equippable */
    public static final int EQUIPPABLE = 5;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static final int VISITED = 1000;
}
