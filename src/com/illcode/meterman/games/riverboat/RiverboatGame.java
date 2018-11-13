package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.Game;
import com.illcode.meterman.WorldState;

public class RiverboatGame implements Game
{
    private static final String NAME = "The Riverboat";
    
    public RiverboatGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.gameName = NAME;
        setupWorldState(worldState);
        return worldState;
    }

    public void about() {

    }

    private void setupWorldState(WorldState worldState) {
        // Do stuff!
    }
}
