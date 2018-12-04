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
    public static final String WORLDBUILDER_KEY = "com.illcode.meterman.impl.WorldBuilder";

    protected WorldState worldState;
    protected TextBundle bundle;

    protected Map<String,BaseEntity> entityIdMap;
    protected Map<String,BaseRoom> roomIdMap;

    // For use with getDoorTextOrDefault()
    protected String[] retrieveMultiStrings;

    // Zero-arg constructor for deserialization
    public WorldBuilder() {
    }

    public WorldBuilder(WorldState worldState, TextBundle bundle) {
        this.worldState = worldState;
        this.bundle = bundle;
        entityIdMap = new HashMap<>(400);
        roomIdMap = new HashMap<>(100);
        GameUtils.ensureBundleHasParent(bundle, Meterman.getSystemBundle());
        worldState.worldData.put(WORLDBUILDER_KEY, this);
    }

    public static WorldBuilder getWorldBuilder() {
        return (WorldBuilder) Meterman.gm.getWorldData().get(WORLDBUILDER_KEY);
    }

    public TextBundle getBundle() {
        return bundle;
    }

    public BaseEntity getEntity(String entityId) {
        return entityIdMap.get(entityId);
    }

    public void putEntity(BaseEntity e) {
        entityIdMap.put(e.id, e);
    }
    
    public void removeEntity(String entityId) {
        entityIdMap.remove(entityId);
    }
    
    public BaseRoom getRoom(String roomId) {
        return roomIdMap.get(roomId);
    }

    public void putRoom(BaseRoom r) {
        roomIdMap.put(r.id, r);
    }
    
    public void removeRoom(String roomId) {
        roomIdMap.remove(roomId);
    }

    public BaseEntity loadEntity(String passageName) {
        BaseEntity e = new BaseEntity();
        e.init();
        readEntityDataFromBundle(e, passageName);
        putEntity(e);
        return e;
    }

    public JsonObject readEntityDataFromBundle(BaseEntity e, String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            e.id = getJsonString(o.get("id"));
            e.name = getJsonString(o.get("name"));
            e.listName = getJsonString(o.get("listName"));
            e.description = getJsonString(o.get("description"));
            return o;
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadEntity()", ex);
            return null;
        }
    }

    public BaseRoom loadRoom(String passageName) {
        BaseRoom r = new BaseRoom();
        r.init();
        readRoomDataFromBundle(r, passageName);
        putRoom(r);
        return r;
    }

    public JsonObject readRoomDataFromBundle(BaseRoom r, String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            r.id = getJsonString(o.get("id"));
            r.name = getJsonString(o.get("name"));
            r.exitName = getJsonString(o.get("exitName"));
            r.description = getJsonString(o.get("description"));
            return o;
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadRoom()", ex);
            return null;
        }
    }

    public DarkRoom loadDarkRoom(String passageName) {
        DarkRoom dr = new DarkRoom();
        dr.init();
        readDarkRoomDataFromBundle(dr, passageName);
        putRoom(dr);
        return dr;
    }

    public void readDarkRoomDataFromBundle(DarkRoom dr, String passageName) {
        JsonObject o = readRoomDataFromBundle(dr, passageName);
        if (o == null)
            return;
        try {
            dr.darkName = retrieveTextOrDefault(o, "darkName", "default-darkName");
            dr.darkExitName = retrieveTextOrDefault(o, "darkExitName", "default-darkExitName");
            dr.darkDescription = retrieveTextOrDefault(o, "darkDescription", "default-darkDescription");
            JsonValue v = o.get("dark");
            if (v != null && v.isBoolean() && v.asBoolean())
                dr.setAttribute(Attributes.DARK);
        } catch (UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readDarkRoomDataFromBundle()", ex);
        }
    }

    public Door loadDoor(String passageName) {
        Door d = new Door();
        d.init();
        readDoorDataFromBundle(d, passageName);
        putEntity(d);
        return d;
    }

    public void readDoorDataFromBundle(Door d, String passageName) {
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
            retrieveMultiTextOrDefault(o, "openMessages", 2, "default-door-open");
            d.setOpenMessages(retrieveMultiStrings[0], retrieveMultiStrings[1]);
        } catch (ParseException |UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadDoor()", ex);
        }
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

    public void connectRoomsWithDoor(String doorId, String roomId1, int pos1, String roomId2, int pos2,
                                     Entity key, boolean locked, boolean open) {
        Door d = (Door) getEntity(doorId);
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.entities.add(d);
        r2.entities.add(d);
        d.setRooms(r1, r2);
        d.setPositions(pos1, pos2);
        d.setKey(key);
        d.setLocked(locked);
        d.setOpen(open);
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

    public Map<String,TalkTopic> loadTopicMap(String passageName) {
        Map<String,TalkTopic> topicMap = new HashMap<>();
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            for (JsonObject.Member member : o) {
                String key = member.getName();
                JsonArray a = member.getValue().asArray();
                String label = getJsonString(a.get(0));
                String text = getJsonString(a.get(1));
                if (!label.isEmpty() && !text.isEmpty())
                    topicMap.put(key, new TalkTopic(key, label, text));
            }
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadDoor()", ex);
        }
        return topicMap;
    }

    protected String retrieveTextOrDefault(JsonObject o, String key, String defaultPassageName) {
        JsonValue v = o.get(key);
        if (v == null)
            return bundle.getPassage(defaultPassageName);
        else
            return getJsonString(v);
    }

    protected void retrieveMultiTextOrDefault(JsonObject o, String key, int numStrings, String defaultPassageName) {
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
     * If <tt>v</tt> is a normal JSON string, return its String value; if <tt>v</tt> is a string of
     * the form <tt>"[[passage-name]]"</tt> use it as a passage name in the text bundle. Otherwise
     * return the empty string.
     */
    protected String getJsonString(JsonValue v) {
        return getJsonString(v, "");
    }

    /**
     * If <tt>v</tt> is a normal JSON string, return its String value; if <tt>v</tt> is a string of
     * the form <tt>"[[passage-name]]"</tt> use it as a passage name in the text bundle. Otherwise
     * return {@code defaultVal}.
     */
    protected String getJsonString(JsonValue v, String defaultVal) {
        if (v == null || !v.isString())
            return defaultVal;
        String s = v.asString();
        if (s.startsWith("[[") && s.endsWith("]]")) {
            s = s.substring(2, s.length() - 2).trim();
            if (s.isEmpty())
                return defaultVal;
            else
                return bundle.getPassage(s);
        } else {
            return s;
        }
    }
}
