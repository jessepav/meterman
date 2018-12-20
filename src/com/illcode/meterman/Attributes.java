package com.illcode.meterman;

import com.illcode.meterman.impl.DarkRoom;

import java.util.LinkedList;
import java.util.List;

/**
 * The system attribute constants used with the Entity and Room attribute methods
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

    /** This entity has a proper name and doesn't require the definite article. */
    public static final int PROPER_NAME = 5;

    //
    // Room attributes
    //

    /** Indicates if a room has been visited before */
    public static final int VISITED = 0;

    /** This room is naturally dark.
     *  @see DarkRoom */
    public static final int DARK = 1;
    
    /**
     * Return a string representation of a given entity attribute.
     * @param attribute the attribute
     * @return string representation, or "unknown" if the attribute is unknown.
     */
    public static String entityAttributeToString(int attribute) {
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
        case PROPER_NAME:
            return "proper-name";
        default:
            return "unknown";
        }
    }

    /**
     * Return a string representation of a given room attribute.
     * @param attribute the attribute
     * @return string representation, or "unknown" if the attribute is unknown.
     */
    public static String roomAttributeToString(int attribute) {
        switch (attribute) {
        case VISITED:
            return "visited";
        case DARK:
            return "dark";
        default:
            return "unknown";
        }
    }

    
    /**
     * Return the entity attribute corresponding to a given string representation.
     * @param s string representation
     * @return attribute corresponding to <tt>s</tt>, or -1 if <tt>s</tt> doesn't correspond
     *         to a known attribute.
     */
    public static int stringToEntityAttribute(String s) {
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
        case "proper-name":
            return PROPER_NAME;
        default:
            return -1;
        }
    }

    /**
     * Return the room attribute corresponding to a given string representation.
     * @param s string representation
     * @return attribute corresponding to <tt>s</tt>, or -1 if <tt>s</tt> doesn't correspond
     *         to a known attribute.
     */
    public static int stringToRoomAttribute(String s) {
        s = s.toLowerCase();
        switch (s) {
        case "visited":
            return VISITED;
        case "dark":
            return DARK;
        default:
            return -1;
        }
    }
}
