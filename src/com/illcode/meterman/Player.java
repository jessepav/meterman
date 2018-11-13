package com.illcode.meterman;

import java.util.LinkedList;
import java.util.List;

/**
 * The player character
 */
public class Player
{
    /** The room in which the player is currently */
    public Room currentRoom;

    /** A list of all entities in the player's inventory */
    public List<Entity> inventory;

    /** A list of entities worn by the player (subset of {@link #inventory}) */
    public List<Entity> worn;

    /** A list of entities equipped by the player (subset of {@link #inventory}) */
    public List<Entity> equipped;

    public Player() {
    }

    public void init() {
        currentRoom = null;
        inventory = new LinkedList<>();
        worn = new LinkedList<>();
        equipped = new LinkedList<>();
    }
}
