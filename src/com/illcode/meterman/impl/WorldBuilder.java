package com.illcode.meterman.impl;

import com.eclipsesource.json.*;
import com.illcode.meterman.*;

import java.util.Arrays;
import java.util.HashMap;
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
        entityIdMap.put(e.id, e);
        return e;
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
        entityIdMap.put(d.id, d);
        return d;
    }

    public void connectRooms(String roomId1, int pos1, String roomId2, int pos2) {
        BaseRoom r1 = roomIdMap.get(roomId1);
        BaseRoom r2 = roomIdMap.get(roomId2);
        r1.exits[pos1] = r2;
        r2.exits[pos2] = r1;
    }

    public void connectRoomsWithDoor(String doorId, String roomId1, int pos1, String roomId2, int pos2, boolean locked) {
        Door d = (Door) entityIdMap.get(doorId);
        BaseRoom r1 = roomIdMap.get(roomId1);
        BaseRoom r2 = roomIdMap.get(roomId2);
        d.setRooms(r1, r2);
        d.setPositions(pos1, pos2);
        d.setLocked(locked);
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
        if (v.isString())
            return v.asString();
        else if (v.isArray())
            return bundle.getPassage(v.asArray().get(0).asString());
        else
            return "";
    }
}
