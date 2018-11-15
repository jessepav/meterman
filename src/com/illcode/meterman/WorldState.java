package com.illcode.meterman;

import com.illcode.meterman.games.GamesList;

import java.util.List;
import java.util.Map;

/**
 * A class representing the entire state of the game world.
 * An instance of this class will be serialized to yield save data.
 */
public class WorldState
{
    /** The name of the game, as found in {@link GamesList}*/
    public String gameName;

    /** The Player character */
    public Player player;

    /** The rooms of this world (which in turn contain its various entities) */
    public List<Room> rooms;

    /** Extra data that objects can use to maintain and communicate state */
    public Map<String,Object> worldData;

    /** The number of turns that have occurred in this world so far */
    public int numTurns;
}
