package com.illcode.riverboat;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.BaseRoom;
import com.illcode.meterman.impl.BasicWorldManager;
import com.illcode.meterman.impl.WorldBuilder;

import java.util.Map;

public class RiverboatGame implements Game
{
    private static final String NAME = "The Riverboat";

    private TextBundle bundle;
    
    public RiverboatGame() {
    }

    public String getName() {
        return NAME;
    }

    public void init() {
        bundle = TextBundle.loadBundle(Utils.pathForGameAsset("riverboat-bundle.txt"));
        Meterman.setGameBundle(bundle);
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.init(NAME);
        Map<String,Object> worldData = worldState.worldData;

        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        wb.saveTo(worldData);

        // Install the state object for part 1
        RiverboatStatePart1 statePart1 = new RiverboatStatePart1();
        statePart1.init();
        statePart1.saveTo(worldData);

        wb.loadRooms("part1-rooms");
        wb.loadEntities("part1-entities");
        wb.loadRoomConnections("part1-connections");
        wb.loadEntityPlacements("part1-entity-placements");
        wb.loadContainerContents("part1-container-contents");
        wb.loadPlayerState("player-state");

        BasicWorldManager bwm = new BasicWorldManager();
        bwm.init();
        bwm.register();
        bwm.saveTo(worldData);

        RiverboatManagerPart1 rm1 = new RiverboatManagerPart1();
        rm1.init(wb, worldData);
        rm1.register();
        rm1.saveTo(worldData);

        return worldState;
    }

    public void about() {
        GameUtils.showPassagesF(bundle, "about-riverboat");
    }

    public void start(boolean newGame) {
    }

    public void dispose() {
        bundle = null;
    }

    public void debugCommand(String command) {

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
