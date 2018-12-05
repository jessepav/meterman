package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.BaseRoom;
import com.illcode.meterman.impl.WorldBuilder;

import java.util.HashMap;
import java.util.Map;

public class RiverboatGame implements Game
{
    private static final String NAME = "The Riverboat";

    private TextBundle bundle;
    
    public RiverboatGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public void init() {

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
        bundle = TextBundle.loadBundle(Utils.pathForAsset("riverboat/riverboat-bundle.txt"), Meterman.getSystemBundle());
        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        worldState.worldData.put("entityIdMap", wb.getEntityIdMap());
        worldState.worldData.put("roomIdMap", wb.getRoomIdMap());

        // Install the state object for part 1
        RiverboatStatePart1 statePart1 = new RiverboatStatePart1();
        statePart1.init();
        statePart1.install(worldState.worldData);
    }

    /**
     * Utility method for retrieving a BaseEntity, which was loaded by the WorldBuilder, by its ID.
     * <p/>
     * This method can only be called once the game is running and the world state returned by
     * {@link #getInitialWorldState()} has been loaded by the GameManager.
     */
    @SuppressWarnings("unchecked")
    static BaseEntity getEntity(String entityId) {
        return ((Map<String,BaseEntity>) Meterman.gm.getWorldData().get("entityIdMap")).get(entityId);
    }

    /**
     * Utility method for retrieving a BaseRoom, which was loaded by the WorldBuilder, by its ID.
     * <p/>
     * This method can only be called once the game is running and the world state returned by
     * {@link #getInitialWorldState()} has been loaded by the GameManager.
     */
    @SuppressWarnings("unchecked")
    static BaseRoom getRoom(String roomId) {
        return ((Map<String,BaseRoom>) Meterman.gm.getWorldData().get("roomIdMap")).get(roomId);
    }

}
