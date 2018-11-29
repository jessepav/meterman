package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.WorldBuilder;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

public class RiverboatGame implements Game
{
    private static final String NAME = "The Riverboat";

    private TextBundle bundle;
    
    public RiverboatGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.gameName = NAME;
        worldState.player = new Player();
        worldState.player.init();
        worldState.worldData = new HashMap<>();
        worldState.numTurns = 0;
        setupWorldState(worldState);
        return worldState;
    }

    public void about() {

    }

    public void start(boolean newGame) {

    }

    public void dispose() {

    }

    private void setupWorldState(WorldState worldState) {
        bundle = TextBundle.loadBundle(Paths.get("assets/riverboat/riverboat-bundle.txt"), Meterman.systemBundle);
        WorldBuilder wb = new WorldBuilder(worldState, bundle);
    }
}
