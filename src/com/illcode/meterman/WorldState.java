package com.illcode.meterman;

import java.util.List;
import java.util.Map;

/**
 * A class representing the entire state of the game world.
 * An instance of this class will be serialized to yield save data.
 */
public class WorldState
{
    /** The Player character */
    public Player player;

    /** The rooms of this world (which in turn contain its various entities) */
    public List<Room> rooms;

    /** Extra data that objects can use to maintain and communicate state */
    public Map<String,Object> data;
}
