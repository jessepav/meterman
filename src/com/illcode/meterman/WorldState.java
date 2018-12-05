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

    /**
     * Extra data that objects can use to maintain and communicate state.
     * <p/>
     * Games should avoid putting any references to external objects (like the system bundle) into
     * the worldData, because it often causes problems with proper serialization and deserialization.
     */
    public Map<String,Object> worldData;

    /** The number of turns that have occurred in this world so far */
    public int numTurns;
}
