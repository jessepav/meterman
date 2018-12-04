package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.WorldBuilder;

import java.util.HashMap;

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
        bundle = TextBundle.loadBundle(Utils.pathForAsset("riverboat/riverboat-bundle.txt"), Meterman.systemBundle);
        WorldBuilder wb = new WorldBuilder(worldState, bundle);

        // Install the state object for part 1
        RiverboatStatePart1 statePart1 = new RiverboatStatePart1();
        statePart1.init();
        statePart1.install();

        // Create the UndergroundHermit sitting by the fire
        UndergroundHermit hermit = new UndergroundHermit();
        hermit.init();
        wb.readEntityDataFromBundle(hermit, "underground-hermit-info");
        hermit.setTopicMap(wb.loadTopicMap("underground-hermit-topics"));

    }
}
