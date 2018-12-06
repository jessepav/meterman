package com.illcode.meterman;

import java.util.BitSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A class representing the entire state of the game world.
 * An instance of this class will be serialized to yield save data.
 * <p/>
 * Games should avoid putting any references to external objects (like the system bundle) into
 * the worldData, because it will cause problems with proper serialization and deserialization.
 * (Particularly, don't save references to <tt>TextBundle</tt>S of any sort in your world graph,
 * because they won't serialize properly.) We can serialize {@link BitSet}S and {@link Pattern}S,
 * but don't go willy-nilly throwing fancy objects into your classes.
 * @see KryoPersistence#init()
 */
public class WorldState
{
    /** The name of the game, as found in {@link GamesList}*/
    public String gameName;

    /** The Player character */
    public Player player;

    /**
     * Extra data that objects can use to maintain and communicate state.
     */
    public Map<String,Object> worldData;

    /** The number of turns that have occurred in this world so far */
    public int numTurns;
}
