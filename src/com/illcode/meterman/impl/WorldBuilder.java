package com.illcode.meterman.impl;

import com.eclipsesource.json.*;
import com.illcode.meterman.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

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

    // For use with getDoorTextOrDefault()
    private String[] retrieveMultiStrings;

    // Zero-arg constructor for deserialization
    public WorldBuilder() {
    }

    public WorldBuilder(WorldState worldState, TextBundle bundle) {
        this.worldState = worldState;
        this.bundle = bundle;
        entityIdMap = new HashMap<>(400);
        roomIdMap = new HashMap<>(100);
        worldState.worldData.put("worldBuilder", this);
    }

    public static WorldBuilder getWorldBuilder(WorldState worldState) {
        return (WorldBuilder) worldState.worldData.get("worldBuilder");
    }

    public TextBundle getBundle() {
        return bundle;
    }

    public BaseEntity getEntity(String entityId) {
        return entityIdMap.get(entityId);
    }

    public void putEntity(String entityId, BaseEntity e) {
        entityIdMap.put(entityId, e);
    }
    
    public void removeEntity(String entityId) {
        entityIdMap.remove(entityId);
    }
    
    public BaseRoom getRoom(String roomId) {
        return roomIdMap.get(roomId);
    }

    public void putRoom(String roomId, BaseRoom e) {
        roomIdMap.put(roomId, e);
    }
    
    public void removeRoom(String roomId) {
        roomIdMap.remove(roomId);
    }

    public BaseEntity loadEntity(String passageName) {
        BaseEntity e = new BaseEntity();
        e.init();
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            e.id = getJsonString(o.get("id"));
            e.name = getJsonString(o.get("name"));
            e.listName = getJsonString(o.get("listName"));
            e.description = getJsonString(o.get("description"));
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadEntity()", ex);
        }
        putEntity(e.id, e);
        return e;
    }

    public BaseRoom loadRoom(String passageName) {
        BaseRoom r = new BaseRoom();
        r.init();
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            r.id = getJsonString(o.get("id"));
            r.name = getJsonString(o.get("name"));
            r.exitName = getJsonString(o.get("exitName"));
            r.description = getJsonString(o.get("description"));
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadRoom()", ex);
        }
        putRoom(r.id, r);
        worldState.rooms.add(r);
        return r;
    }

    public Door loadDoor(String passageName) {
        Door d = new Door();
        d.init();
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            d.id = getJsonString(o.get("id"));
            d.name = getJsonString(o.get("name"));
            d.listName = getJsonString(o.get("listName"));
            retrieveMultiTextOrDefault(o, "descriptions", 2, "default-door-description");
            d.setDescriptions(retrieveMultiStrings[0], retrieveMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "lockedMessages", 2, "default-door-locked");
            d.setLockedMessages(retrieveMultiStrings[0], retrieveMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "unlockedMessages", 2, "default-door-unlocked");
            d.setUnlockedMessages(retrieveMultiStrings[0], retrieveMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "noKeyMessages", 2, "default-door-nokey");
            d.setNoKeyMessages(retrieveMultiStrings[0], retrieveMultiStrings[1]);
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadDoor()", ex);
        }
        putEntity(d.id, d);
        return d;
    }

    public void connectRooms(String roomId1, int pos1, String roomId2, int pos2) {
        connectRooms(roomId1, pos1, null, roomId2, pos2, null);
    }

    public void connectRooms(String roomId1, int pos1, String label1, String roomId2, int pos2, String label2) {
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.exits[pos1] = r2;
        r1.exitLabels[pos1] = label1;
        r2.exits[pos2] = r1;
        r2.exitLabels[pos2] = label2;
    }

    public void connectRoomOneWay(String roomId1, int pos1, String roomId2) {
        connectRoomOneWay(roomId1, pos1, null, roomId2);
    }

    public void connectRoomOneWay(String roomId1, int pos1, String label, String roomId2) {
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.exits[pos1] = r2;
        r1.exitLabels[pos1] = label;
    }

    public void connectRoomsWithDoor(String doorId, String roomId1, int pos1, String roomId2, int pos2, boolean locked) {
        Door d = (Door) getEntity(doorId);
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        d.setRooms(r1, r2);
        d.setPositions(pos1, pos2);
        d.setLocked(locked);
        // Exit labels don't work with rooms that are connected by a door
        r1.exitLabels[pos1] = null;
        r2.exitLabels[pos2] = null;
        if (locked) {
            r1.exits[pos1] = null;
            r2.exits[pos2] = null;
        } else {
            r1.exits[pos1] = r2;
            r2.exits[pos2] = r1;
        }
    }

    public void putEntitiesInRoom(String roomId, String... entityIds) {
        BaseRoom r = getRoom(roomId);
        for (String id : entityIds) {
            Entity e = getEntity(id);
            if (!r.entities.contains(e))
                r.entities.add(e);
        }
    }

    private String retrieveTextOrDefault(JsonObject o, String key, String defaultPassageName) {
        JsonValue v = o.get(key);
        if (v == null)
            return bundle.getPassage(defaultPassageName);
        else
            return getJsonString(v);
    }

    private void retrieveMultiTextOrDefault(JsonObject o, String key, int numStrings, String defaultPassageName) {
        if (retrieveMultiStrings == null || retrieveMultiStrings.length < numStrings)
            retrieveMultiStrings = new String[numStrings];
        JsonValue v = o.get(key);
        if (v == null) {
            Arrays.fill(retrieveMultiStrings, 0, numStrings, bundle.getPassage(defaultPassageName));
        } else {
            JsonArray a = v.asArray();
            if (a.size() == 0) {
                Arrays.fill(retrieveMultiStrings, 0, numStrings, "");
            } else if (a.size() < numStrings) {
                Arrays.fill(retrieveMultiStrings, 0, numStrings, getJsonString(a.get(0)));
            } else {
                for (int i = 0; i < numStrings; i++)
                    retrieveMultiStrings[i] = getJsonString(a.get(i));
            }
        }
    }

    /**
     * If <tt>v</tt> is a JSON string, return its String value; if <tt>v</tt> is a JSON array,
     * take its first item as a string, and use it as a passage name in the text bundle.
     */
    private String getJsonString(JsonValue v) {
        if (v == null)
            return "";
        else if (v.isString())
            return v.asString();
        else if (v.isArray())
            return bundle.getPassage(v.asArray().get(0).asString());
        else
            return "";
    }
}
