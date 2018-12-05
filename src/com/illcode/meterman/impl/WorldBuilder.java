package com.illcode.meterman.impl;

import com.eclipsesource.json.*;
import com.illcode.meterman.*;
import com.illcode.meterman.ui.MetermanUI;

import java.util.*;
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

    public void init(WorldState worldState, TextBundle bundle) {
        this.worldState = worldState;
        this.bundle = bundle;
        entityIdMap = new HashMap<>(400);
        roomIdMap = new HashMap<>(100);
        GameUtils.ensureBundleHasParent(bundle, Meterman.getSystemBundle());
    }

    public void install() {
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

    /**
     * Loads an entity from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
    {
        "id" : "ferryman",
        "name" : "River Ferryman",
        "listName" : "Ferryman",
        "description" : "[[ferryman-description]]",
        "imageName" : "ferryman",
    }
     * }</pre>
     * @param e BaseEntity into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
    public JsonObject readEntityDataFromBundle(BaseEntity e, String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            e.id = getJsonString(o.get("id"), passageName);
            e.name = getJsonString(o.get("name"), passageName);
            e.listName = getJsonString(o.get("listName"), passageName);
            e.description = getJsonString(o.get("description"), passageName);
            e.imageName = jsonValueAsString(o.get("imageName"), MetermanUI.NO_IMAGE);
            return o;
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readEntityDataFromBundle()", ex);
            return null;
        }
    }

    public TalkingEntity loadTalkingEntity(String passageName) {
        TalkingEntity te = new TalkingEntity();
        te.init();
        readTalkingEntityDataFromBundle(te, passageName);
        putEntity(te);
        return te;
    }

    /**
     * Loads a talking entity from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
    {
        "id" : "underground-hermit",
        "name" : "Bearded Hermit",
        "listName" : "Hermit",
        "description" : "[[underground-hermit-description]]",
        "imageName" : "underground-hermit",
        "dialogText" : "The hermit waits for you to say something.",
        "topicMap" : "underground-hermit-topics",
        "currentTopics" : ["hello"]
    }
     * }</pre>
     * @param te TalkingEntity into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
    public JsonObject readTalkingEntityDataFromBundle(TalkingEntity te, String passageName) {
        JsonObject o = readEntityDataFromBundle(te, passageName);
        if (o == null)
            return null;
        try {
            te.dialogText = retrieveTextOrDefault(o, "dialogText", "default-TalkingEntity-dialogText");
            te.topicMap = loadTopicMap(o.get("topicMap").asString());
            JsonValue v = o.get("currentTopics");
            if (v != null && v.isArray()) {
                for (JsonValue topic : v.asArray().values())
                    te.currentTopics.add(te.topicMap.get(topic.asString()));
            }
            return o;
        } catch (UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readTalkingEntityDataFromBundle()", ex);
            return null;
        }
    }

    public Map<String,TalkTopic> loadTopicMap(String passageName) {
        Map<String,TalkTopic> topicMap = new HashMap<>();
        String json = bundle.getPassage(passageName);
        try {
            JsonObject topicMapObj = Json.parse(json).asObject();
            // On the first pass we just gather up the keys, labels, and text, and put
            // the resulting TalkTopic instances into the topicMap.
            for (JsonObject.Member member : topicMapObj) {
                JsonObject topicObj = member.getValue().asObject();
                TalkTopic tt = new TalkTopic();
                tt.key = member.getName();
                tt.label = getJsonString(topicObj.get("label"), "(label)");
                tt.text = getJsonString(topicObj.get("text"), "(text)");
                topicMap.put(tt.key, tt);
            }
            // On the second pass we read the addTopics and removeTopics lists and
            // weave together the topic graph.
            for (JsonObject.Member member : topicMapObj) {
                JsonObject topicObj = member.getValue().asObject();
                TalkTopic tt = topicMap.get(member.getName());
                JsonValue v = topicObj.get("addTopics");
                if (v != null && v.isArray()) {
                    JsonArray arr = v.asArray();
                    tt.addTopics = new ArrayList<>(arr.size());
                    for (JsonValue topicKey : arr.values())
                        tt.addTopics.add(topicMap.get(topicKey.asString()));
                } else {
                    tt.addTopics = Collections.emptyList();
                }
                v = topicObj.get("removeTopics");
                if (v != null && v.isArray()) {
                    JsonArray arr = v.asArray();
                    tt.removeTopics = new ArrayList<>(arr.size());
                    for (JsonValue topicKey : arr.values())
                        tt.removeTopics.add(topicMap.get(topicKey.asString()));
                } else {
                    tt.removeTopics = Collections.emptyList();
                }
            }
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadDoor()", ex);
        }
        return topicMap;
    }

    public Door loadDoor(String passageName) {
        Door d = new Door();
        d.init();
        readDoorDataFromBundle(d, passageName);
        putEntity(d);
        return d;
    }

    /**
     * Loads a door from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
        {
            "id" : "river-edge-grating",
            "name" : "Iron Grating",
            "listName" : "Grating",
            "imageName" : "river-edge-grating",
            "descriptions" : [
                "A strong, oddly fresh iron grating is in the soil 30 meters from the bank.",
                "Above you is an iron grating."
            ],
            "lockedMessages" : [
                "The grating is secured by a padlock.",
                "A padlock can be seen just through the bars."
            ],
            "unlockedMessages" : [
                "A padlock lies opened on the grating.",
                "An open padlock can be seen through the bars."
            ],
            "noKeyMessages" : [
                "You do not have the key for the padlock."
            ],
            "openMessages" : [
                "The grate is open.",
                "You can see the sky through the open grate."
            ]
        }
     * }</pre>
     * @param d Door into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
    public JsonObject readDoorDataFromBundle(Door d, String passageName) {
        JsonObject o = readEntityDataFromBundle(d, passageName);
        if (o == null)
            return null;
        try {
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
            return o;
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readDoorDataFromBundle()", ex);
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

    /**
     * Loads a room from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
       {
           "id" : "river-edge",
           "name" : "River's Edge",
           "exitName" : "River Edge",
           "description" : "Here the woods give way to the bank of the River Jelly."
       }
     * }</pre>
     * @param r BaseRoom into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
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
            logger.log(Level.WARNING, "JSON error, readRoomDataFromBundle()", ex);
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

    /**
     * Loads a dark room from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
        {
            "id" : "dark-passage1",
            "name" : "Dark Passage",
            "exitName" : "Dark Passage",
            "description" : "A stony underground passage."
            "darkName" : "Dark Underground Passage",
            "darkExitName" : "Darkness",
            "darkDescription" : "The air is still and slightly acrid in this dark passage.",
            "dark" : true,
        }
     * }</pre>
     * @param dr DarkRoom into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
    public JsonObject readDarkRoomDataFromBundle(DarkRoom dr, String passageName) {
        JsonObject o = readRoomDataFromBundle(dr, passageName);
        if (o == null)
            return null;
        try {
            dr.darkName = retrieveTextOrDefault(o, "darkName", "default-darkName");
            dr.darkExitName = retrieveTextOrDefault(o, "darkExitName", "default-darkExitName");
            dr.darkDescription = retrieveTextOrDefault(o, "darkDescription", "default-darkDescription");
            JsonValue v = o.get("dark");
            if (v != null && v.isBoolean() && v.asBoolean())
                dr.setAttribute(Attributes.DARK);
            return o;
        } catch (UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readDarkRoomDataFromBundle()", ex);
            return null;
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
        if (v == null || !v.isArray()) {
            Arrays.fill(retrieveMultiStrings, 0, numStrings, bundle.getPassage(defaultPassageName));
        } else {
            JsonArray arr = v.asArray();
            if (arr.size() == 0) {
                Arrays.fill(retrieveMultiStrings, 0, numStrings, "");
            } else if (arr.size() < numStrings) {
                Arrays.fill(retrieveMultiStrings, 0, numStrings, getJsonString(arr.get(0)));
            } else {
                for (int i = 0; i < numStrings; i++)
                    retrieveMultiStrings[i] = getJsonString(arr.get(i));
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
     * return {@code defaultString}.
     */
    protected String getJsonString(JsonValue v, String defaultString) {
        if (v == null || !v.isString())
            return defaultString;
        String s = v.asString();
        if (s.startsWith("[[") && s.endsWith("]]")) {
            s = s.substring(2, s.length() - 2).trim();
            if (s.isEmpty())
                return defaultString;
            else
                return bundle.getPassage(s);
        } else {
            return s;
        }
    }

    /**
     * If <tt>v</tt> is a string, return its value; otherwise return <tt>defaultString</tt>.
     */
    protected String jsonValueAsString(JsonValue v, String defaultString) {
        if (v == null || !v.isString())
            return defaultString;
        else
            return v.asString();
    }
}
