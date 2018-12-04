package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.Meterman;

public class RiverboatStatePart1
{
    public static final String RIVERBOAT_STATE_PART1_KEY = "com.illcode.meterman.games.riverboat.RiverboatStatePart1";

    public boolean monsterSeen; // has the player encountered the monster in the dark?

    public void init() {
        monsterSeen = false;
    }

    public void install() {
        Meterman.gm.getWorldData().put(RIVERBOAT_STATE_PART1_KEY, this);
    }

    public static RiverboatStatePart1 getRiverboatStatePart1() {
        return (RiverboatStatePart1) Meterman.gm.getWorldData().get(RIVERBOAT_STATE_PART1_KEY);
    }
}
