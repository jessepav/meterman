package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.ClassMapper;
import com.illcode.meterman.Game;
import com.illcode.meterman.WorldState;

public class RiverboatGame implements Game
{
    private static final String NAME = "The Riverboat";
    
    private RiverboatClassMapper classMapper;

    public RiverboatGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public ClassMapper getClassMapper() {
        if (classMapper == null)
            classMapper = new RiverboatClassMapper();
        return classMapper;
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.gameName = NAME;
        setupWorldState(worldState);
        return worldState;
    }

    private void setupWorldState(WorldState worldState) {
        // Do stuff!
    }
}
