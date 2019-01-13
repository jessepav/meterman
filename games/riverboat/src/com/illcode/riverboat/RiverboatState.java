package com.illcode.riverboat;

import java.util.Map;

/**
 * The various bits of state used to communicate amongst the denizens of the world
 * of the Riverboat. This can be thought of as a container of global variables.
 */
public class RiverboatState
{
    public static final String RIVERBOAT_STATE_KEY = "RiverboatState";

    // PART 1
    public boolean animalsInGlade;  // are the animals still in the glade?
    public boolean malibuLightOn;  // is the malibu light in the glade on?
    public boolean monsterSeen; // has the player encountered the monster in the dark?
    public int monsterCounter;  // the # of turns the monster has remaining in this dimension (0 = no monster)

    public void init() {
        monsterSeen = false;
    }

    public void saveTo(Map<String,Object> worldData) {
        worldData.put(RIVERBOAT_STATE_KEY, this);
    }

    public static RiverboatState retrieveFrom(Map<String,Object> worldData) {
        return (RiverboatState) worldData.get(RIVERBOAT_STATE_KEY);
    }
}
