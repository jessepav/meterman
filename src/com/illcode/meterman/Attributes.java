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
    
    /**
     * Return a string representation of a given attribute.
     * @param attribute the attribute
     * @return string representation, or "unknown" if the attribute is unknown.
     */
    public static String attributeToString(int attribute) {
        switch (attribute) {
        case CONCEALED:
            return "concealed";
        case TAKEABLE:
            return "takeable";
        case WEARABLE:
            return "wearable";
        case EQUIPPABLE:
            return "equippable";
        case LIGHTSOURCE:
            return "lightsource";
        case DARK:
            return "dark";
        case VISITED:
            return "visited";
        default:
            return "unknown";
        }
    }
    
    /**
     * Return the attribute corresponding to a given string representation.
     * @param s string representation
     * @return attribute corresponding to <tt>s</tt>, or -1 if <tt>s</tt> doesn't correspond
     *         to a known attribute.
     */
    public static int stringToAttribute(String s) {
        s = s.toLowerCase();
        switch (s) {
        case "concealed":
            return CONCEALED;
        case "takeable":
            return TAKEABLE;
        case "wearable":
            return WEARABLE;
        case "equippable":
            return EQUIPPABLE;
        case "lightsource":
            return LIGHTSOURCE;
        case "visited":
            return VISITED;
        case "dark":
            return DARK;
        default:
            return -1;
        }
    }

}
