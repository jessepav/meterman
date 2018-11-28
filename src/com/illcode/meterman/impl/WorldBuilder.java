package com.illcode.meterman.impl;

import com.illcode.meterman.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A class with utility methods to assist generating the {@link Game#getInitialWorldState() initial world state}
 * of a game, using the {@link BaseEntity} and {@link BaseRoom} implementations.
 */
public class WorldBuilder
{
    private WorldState worldState;
    private TextBundle bundle;

    private Map<String,BaseEntity> entityIdMap;
    private Map<String,BaseRoom> roomIdMap;

    public WorldBuilder(WorldState worldState, TextBundle bundle) {
        this.worldState = worldState;
        this.bundle = bundle;
        entityIdMap = new HashMap<>(400);
        roomIdMap = new HashMap<>(100);
    }

    public WorldState getWorldState() {
        return worldState;
    }

    public TextBundle getBundle() {
        return bundle;
    }

    /**
     * Once all rooms have been constructed by the WorldBuilder, this will go through
     * those whose exits were specified by JSON data and link the object graph.
     */
    public void connectRooms() {

    }
}
