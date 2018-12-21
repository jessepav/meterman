package com.illcode.meterman.impl;

import com.eclipsesource.json.*;
import com.illcode.meterman.*;
import com.illcode.meterman.ui.UIConstants;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.illcode.meterman.Utils.logger;

/**
 * A class with utility methods to assist generating the {@link Game#getInitialWorldState() initial world state}
 * of a game, using the {@link BaseEntity} and {@link BaseRoom} implementations.
 * <p/>
 * If you use the "mass effect" methods to load your rooms and entities, the order of
 * calls with usually be
 * <ol>
 *     <li>{@link #loadRooms(String)}</li>
 *     <li>{@link #loadEntities(String)}</li>
 *     <li>{@link #loadRoomConnections(String)}</li>
 *     <li>{@link #loadEntityPlacements(String)}</li>
 *     <li>{@link #loadContainerContents(String)}</li>
 *     <li>{@link #loadPlayerState(String)}</li>
 * </ol>
 * In this case, only delegates and managers will need to be manually wired together in a Game's
 * {@code getInitialWorldState()} method.
 */
public class WorldBuilder
{
    public static final String WORLDBUILDER_KEY = "com.illcode.meterman.impl.WorldBuilder";

    protected WorldState worldState;
    protected TextBundle bundle;

    protected Map<String,BaseEntity> entityIdMap;
    protected Map<String,BaseRoom> roomIdMap;

    // For use with retrieveMultiTextOrDefault()
    protected String[] retrievedMultiStrings;

    private static Pattern escapedNewlinesPattern;  // see foldEscapedNewlines()


    // Zero-arg constructor for deserialization
    public WorldBuilder() {
    }

    public WorldBuilder(WorldState worldState, TextBundle bundle) {
        this.worldState = worldState;
        this.bundle = bundle;
        entityIdMap = new HashMap<>(400);
        roomIdMap = new HashMap<>(100);
        GameUtils.ensureBundleHasParent(bundle, Meterman.getSystemBundle());
    }

    /** Save this instance into a world-data map. */
    public void saveTo(Map<String,Object> worldData) {
        worldData.put(WORLDBUILDER_KEY, this);
    }

    /** Retrieve the WorldBuilder instance stored by {@link #saveTo(Map)} from worldData. */
    public static WorldBuilder retrieveFrom(Map<String,Object> worldData) {
        return (WorldBuilder) worldData.get(WORLDBUILDER_KEY);
    }

    /**
     * Return an entity loaded by or put into this WorldBuilder.
     * @param entityId unique entity id
     * @return BaseEntity with id = entityId
     */
    public BaseEntity getEntity(String entityId) {
        return entityIdMap.get(entityId);
    }

    /**
     * Put an entity into this WorldBuilder's records.
     * @param e BaseEntity to put in
     */
    public void putEntity(BaseEntity e) {
        entityIdMap.put(e.id, e);
    }

    /**
     * Remove an entity from this WorldBuilder's records.
     * @param entityId id of the BaseEntity to remove
     */
    public void removeEntity(String entityId) {
        entityIdMap.remove(entityId);
    }

    /**
     * Return a room loaded by or put into this WorldBuilder.
     * @param roomId unique room id
     * @return BaseRoom with id = roomId
     */
    public BaseRoom getRoom(String roomId) {
        return roomIdMap.get(roomId);
    }

    /**
     * Put a room into this WorldBuilder's records.
     * @param r BaseRoom to put in
     */
    public void putRoom(BaseRoom r) {
        roomIdMap.put(r.id, r);
    }

    /**
     * Remove a room from this WorldBuilder's records.
     * @param roomId id of the BaseRoom to remove
     */
    public void removeRoom(String roomId) {
        roomIdMap.remove(roomId);
    }

    /**
     * Returns a map from an entity ID to the corresponding BaseEntity.
     * <p/>
     * The map only has entries for those entities loaded by the WorldBuilder or
     * manually inserted via {@link #putEntity(BaseEntity)}.
     * @return entity-id map
     */
    public Map<String,BaseEntity> getEntityIdMap() {
        return entityIdMap;
    }

    /**
     * Returns a map from a room ID to the corresponding BaseRoom
     * <p/>
     * The map only has entries for those rooms loaded by the WorldBuilder or
     * manually inserted via {@link #putRoom(BaseRoom)}.
     * @return room-id map
     */
    public Map<String,BaseRoom> getRoomIdMap() {
        return roomIdMap;
    }

    /**
     * Load and return a BaseEntity from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded BaseEntity
     */
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
            "indefiniteArticle" : "a",
            "listName" : "Ferryman",
            "description" : "[[ferryman-description]]",
            "imageName" : "ferryman",
            "attributes" : ["concealed", "lightsource"],
        }
     * }</pre>
     * <tt>id</tt> is optional; if not present, the name of the passage will be used.<br/>
     * <tt>indefiniteArticle</tt> is optional.<br/>
     * <tt>listName</tt> is optional. If omitted (null) <tt>BaseEntity</tt>
     *      defaults to using the value of <tt>name</tt>.<br/>
     * <tt>imageName</tt> is optional.<br/>
     * <tt>attributes</tt> is optional.
     * <p/>
     * The JSON accepted by this method differs from the standard in one respect: if a line in
     * the input string ends with a backslash, <tt>'\'</tt>, it will be joined
     * to the next line (with leading whitespace removed). In this way we can have multi-line JSON strings.
     * @param e BaseEntity into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     * @see #getEntityAttributeVal(String)
     */
    public JsonObject readEntityDataFromBundle(BaseEntity e, String passageName) {
        String json = foldEscapedNewlines(bundle.getPassage(passageName));
        try {
            JsonObject o = Json.parse(json).asObject();
            e.id = getJsonString(o.get("id"), passageName);
            e.name = getJsonString(o.get("name"));
            e.indefiniteArticle = getJsonString(o.get("indefiniteArticle"), null);
            e.listName = getJsonString(o.get("listName"), null);
            e.description = getJsonString(o.get("description"));
            e.imageName = jsonValueAsString(o.get("imageName"), UIConstants.NO_IMAGE);
            JsonValue v = o.get("attributes");
            if (v != null) {
                for (JsonValue attrVal : v.asArray().values()) {
                    int attr = getEntityAttributeVal(attrVal.asString());
                    if (attr != -1)
                        e.setAttribute(attr);
                }
            }
            return o;
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readEntityDataFromBundle()", ex);
            return null;
        }
    }

    /**
     * Load and return a TalkingEntity from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded TalkingEntity
     */
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
        "noTopicsText" : "The hermit has nothing to say to you.",
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
            te.noTopicsText = retrieveTextOrDefault(o, "noTopicsText", "default-noTopicsText");
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

    /**
     * Loads a "topic map" (i.e. a mapping from topic ID to {@link TalkTopic} instance) from JSON data in the bundle.
     * The format of the topic map JSON data is as in this example:
     * <pre>{@code
        {
            "hello" : {
                "label" : "Hello",
                "text" : "Hello stranger.",
                "addTopics" : ["underground", "monster"]
            },
            "underground" : {
                "label" : "What's this underground?",
                "text" : "The underground area is my refuge from the corrupt world.",
                "addTopics" : ["name"],
                "removeTopics" : ["hello", "underground"]
            },
            "name" : {
                "label" : "What's your name?",
                "text" : "My name is Superfly Hermit!",
                "removeTopics" : ["name"]
            },
            "monster" : {
                "label" : "Monster in the Dark",
                "text" : "[[monster-in-dark-text]]"
            }
        }
     * }</pre>
     * The JSON accepted by this method differs from the standard in one respect: if a line in
     * the input string ends with a backslash, <tt>'\'</tt>, it will be joined
     * to the next line (with leading whitespace removed). In this way we can have multi-line JSON strings.
     * @param passageName name of the passage under which the JSON data is to be found
     * @return topic map
     */
    public Map<String,TalkTopic> loadTopicMap(String passageName) {
        Map<String,TalkTopic> topicMap = new HashMap<>();
        String json = foldEscapedNewlines(bundle.getPassage(passageName));
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

    /**
     * Load and return a Door from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded Door
     */
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
            <standard entity fields>
            "descriptions" : [
                "Description as seen from room #1",
                "Description as seen from room #2"
            ],
            "lockedMessages" : [
                "Additional description shown from room #1 when the door is locked",
                "Additional description shown from room #2 when the door is locked"
            ],
            "unlockedMessages" : [
                "Additional description shown from room #1 when the door is closed but unlocked",
                "Additional description shown from room #2 when the door is closed but unlocked"
            ],
            "openMessages" : [
                "Additional description shown from room #1 when the door is open.",
                "Additional description shown from room #2 when the door is open."
            ],
            "noKeyMessages" : [
                "Message displayed when the player attempts to unlock the door from room #1 without the key",
                "Message displayed when the player attempts to unlock the door from room #2 without the key"
            ],
            "keyId" : entity ID of the key needed to lock/unlock the door, or null for no key,
            "locked" : if key != null, then a boolean to indicate if the door is locked,
            "open" : a boolean to indicate if the door is open
        }
     * }</pre>
     * For each of the above arrays, you can have only one string item, in which case it will be used
     * for both rooms. If the entry is missing completely, a default string from the system bundle
     * will be used.
     * <p/>
     * Since <tt>keyId</tt> will be looked up to resolve an actual Entity instance, the key must have
     * already been loaded prior to loading this door.
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
            d.setDescriptions(retrievedMultiStrings[0], retrievedMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "lockedMessages", 2, "default-door-locked");
            d.setLockedMessages(retrievedMultiStrings[0], retrievedMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "unlockedMessages", 2, "default-door-unlocked");
            d.setUnlockedMessages(retrievedMultiStrings[0], retrievedMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "noKeyMessages", 2, "default-door-nokey");
            d.setNoKeyMessages(retrievedMultiStrings[0], retrievedMultiStrings[1]);
            retrieveMultiTextOrDefault(o, "openMessages", 2, "default-door-open");
            d.setOpenMessages(retrievedMultiStrings[0], retrievedMultiStrings[1]);
            JsonValue v = o.get("keyId");
            if (v != null && !v.isNull()) {
                d.setKey(getEntity(v.asString()));
                v = o.get("locked");
                if (v != null)
                    d.setLocked(v.asBoolean());
            }
            v = o.get("open");
            if (v != null)
                d.setOpen(v.asBoolean());
            return o;
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readDoorDataFromBundle()", ex);
            return null;
        }
    }

    /**
     * Load a {@link Container} from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded Container
     */
    public Container loadContainer(String passageName) {
        Container c = new Container();
        c.init();
        readContainerDataFromBundle(c, passageName);
        putEntity(c);
        return c;
    }

    /**
     * Loads a container from a bundle passage that contains JSON data in the format of this example:
     * <pre>{@code
     *  {
     *      <standard Entity fields>
     *      "inPrep" : preposition used when putting something into the container,
     *      "outPrep" : preposition used when taking something out of the container,
     *      "keyId" : entity ID of the key needed to lock/unlock the container, or null for no key,
     *      "locked" : if key != null, then a boolean to indicate if the container is locked
     *  }
     * }</pre>
     * Since <tt>keyId</tt> will be looked up to resolve an actual Entity instance, the key must have
     * already been loaded prior to loading this container.
     * @param c Container into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     */
    public JsonObject readContainerDataFromBundle(Container c, String passageName) {
        JsonObject o = readEntityDataFromBundle(c, passageName);
        if (o == null)
            return null;
        try {
            c.inPrep = getJsonString(o.get("inPrep"), "(in)");
            c.outPrep = getJsonString(o.get("outPrep"), "(out)");
            JsonValue v = o.get("keyId");
            if (v != null && !v.isNull()) {
                c.key = getEntity(v.asString());
                v = o.get("locked");
                if (v != null)
                    c.locked = v.asBoolean();
            }
            return o;
        } catch (UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readContainerDataFromBundle()", ex);
            return null;
        }
    }

    /**
     * Load and return a BaseRoom from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded BaseRoom
     */
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
           "description" : "Here the woods give way to the bank of the River Jelly.",
           "attributes" : ["dark", "visited"]
       }
     * }</pre>
     * <tt>id</tt> is optional; if not present, the name of the passage will be used.<br/>
     * <tt>exitName</tt> is optional. If omitted (null) <tt>BaseRoom</tt>
     *      defaults to using the value of <tt>name</tt>.<br/>
     * <tt>attributes</tt> is optional.
     * <p/>
     * The JSON accepted by this method differs from the standard in one respect: if a line in
     * the input string ends with a backslash, <tt>'\'</tt>, it will be joined
     * to the next line (with leading whitespace removed). In this way we can have multi-line JSON strings.
     * @param r BaseRoom into which to store the data
     * @param passageName name of the bundle passage
     * @return the JsonObject parsed from <tt>passageName</tt>
     * @see #getRoomAttributeVal(String)
     */
    public JsonObject readRoomDataFromBundle(BaseRoom r, String passageName) {
        String json = foldEscapedNewlines(bundle.getPassage(passageName));
        try {
            JsonObject o = Json.parse(json).asObject();
            r.id = getJsonString(o.get("id"), passageName);
            r.name = getJsonString(o.get("name"));
            r.exitName = getJsonString(o.get("exitName"), null);
            r.description = getJsonString(o.get("description"));
            JsonValue v = o.get("attributes");
            if (v != null) {
                for (JsonValue attrVal : v.asArray().values()) {
                    int attr = getRoomAttributeVal(attrVal.asString());
                    if (attr != -1)
                        r.setAttribute(attr);
                }
            }
            return o;
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readRoomDataFromBundle()", ex);
            return null;
        }
    }

    /**
     * Load and return a DarkRoom from JSON data in the bundle
     * @param passageName name of the passage under which the JSON data is to be found
     * @return loaded DarkRoom
     */
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
            "attributes" : ["dark"]
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
            return o;
        } catch (UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, readDarkRoomDataFromBundle()", ex);
            return null;
        }
    }

    /**
     * Connect two rooms, using the default {@link Room#getExitName()} labels.
     * @param roomId1 id of the first room
     * @param pos1 the exit position in the first room to connect to the second room
     * @param roomId2 id of the second room
     * @param pos2 the exit position in the second room to connect to the first room
     */
    public void connectRooms(String roomId1, int pos1, String roomId2, int pos2) {
        connectRooms(roomId1, pos1, null, roomId2, pos2, null);
    }

    /**
     * Connect two rooms, specifying the exit labels.
     * @param roomId1 id of the first room
     * @param pos1 the exit position in the first room to connect to the second room
     * @param label1 the exit label to use in the first room
     * @param roomId2 id of the second room
     * @param pos2 the exit position in the second room to connect to the first room
     * @param label2 the exit label to use in the second room
     */
    public void connectRooms(String roomId1, int pos1, String label1, String roomId2, int pos2, String label2) {
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.exits[pos1] = r2;
        r1.exitLabels[pos1] = label1;
        r2.exits[pos2] = r1;
        r2.exitLabels[pos2] = label2;
    }

    /**
     * Make a one-way connection, using the default {@link Room#getExitName()} label.
     * @param roomId1 room to connect from
     * @param pos1 exit position to connect
     * @param roomId2 room to connect to
     */
    public void connectRoomsOneWay(String roomId1, int pos1, String roomId2) {
        connectRoomsOneWay(roomId1, pos1, null, roomId2);
    }

    /**
     * Make a one-way connection, specifying the exit label.
     * @param roomId1 room to connect from
     * @param pos1 exit position to connect
     * @param label exit label
     * @param roomId2 room to connect to
     */
    public void connectRoomsOneWay(String roomId1, int pos1, String label, String roomId2) {
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.exits[pos1] = r2;
        r1.exitLabels[pos1] = label;
    }

    /**
     * Connect two rooms with a door between them.
     * @param doorId id of the Door entity
     * @param roomId1 id of the first room
     * @param pos1 the exit position in the first room to connect to the second room
     * @param roomId2 id of the second room
     * @param pos2 the exit position in the second room to connect to the first room
     */
    public void connectRoomsWithDoor(String doorId, String roomId1, int pos1, String roomId2, int pos2) {
        Door d = (Door) getEntity(doorId);
        BaseRoom r1 = getRoom(roomId1);
        BaseRoom r2 = getRoom(roomId2);
        r1.entities.add(d);
        r2.entities.add(d);
        d.setRooms(r1, r2);
        d.setPositions(pos1, pos2);
        // Exit labels don't work with rooms that are connected by a door
        r1.exitLabels[pos1] = null;
        r2.exitLabels[pos2] = null;
        if (d.isLocked()) {
            r1.exits[pos1] = null;
            r2.exits[pos2] = null;
        } else {
            r1.exits[pos1] = r2;
            r2.exits[pos2] = r1;
        }
    }

    /**
     * Put multiple entities into a room.
     * @param roomId id of the room
     * @param entityIds IDs of the entities to put in the room
     */
    public void putEntitiesInRoom(String roomId, String... entityIds) {
        BaseRoom r = getRoom(roomId);
        for (String id : entityIds) {
            Entity e = getEntity(id);
            if (e != null && !r.entities.contains(e)) {
                r.entities.add(e);
                e.setRoom(r);
            }
        }
    }

    /**
     * Loads rooms listed in a JSON definition in a bundle passage. A definition entry looks like this:
     * <pre>{@code
     *   [
     *      passageName1, passageName2, ...
     *   ]
     * }</pre>
     * (you need to format the JSON array in this way because if it's on one line it will
     * be treated as a bundle passage name)
     * <p/>
     * Certain suffixes may be attacked to a passage name for special treatment:
     * <ul>
     *     <li>Suffix <tt>:dark</tt> - loaded as a {@link #loadDarkRoom(String) DarkRoom}</li>
     * </ul>
     * Subclasses of WorldBuilder may allow additional suffix strings not listed here.
     * <p/>
     * For each passage name listed, we will call the appropriate variant of {@link #loadRoom(String)}.
     * @param passageName name of the passage under which the JSON definition is to be found
     * @see #loadRoomType(String, String)
     */
    public void loadRooms(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonArray passageList = Json.parse(json).asArray();
            for (JsonValue v : passageList) {
                String p = v.asString();
                int idx = p.indexOf(':');
                if (idx == -1)
                    loadRoomType(p, "");
                else
                    loadRoomType(p.substring(0, idx), p.substring(idx + 1));
            }
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadRooms()", ex);
        }
    }

    /**
     * Loads entities listed in a JSON definition in a bundle passage. A definition entry looks like this:
     * <pre>{@code
     *   [
     *      passageName1, passageName2, ...
     *   ]
     * }</pre>
     * (you need to format the JSON array in this way because if it's on one line it will
     * be treated as a bundle passage name)
     * <p/>
     * Certain suffixes may be attacked to a passage name for special treatment:
     * <ul>
     *     <li>Suffix <tt>:door</tt> - loaded as a {@link #loadDoor(String) Door}</li>
     *     <li>Suffix <tt>:talking</tt> - loaded as a {@link #loadTalkingEntity(String) TalkingEntity}</li>
     *     <li>Suffix <tt>:container</tt> - loaded as a {@link #loadContainer(String) Container}.<br/>
     *          If the container definition has a <tt>keyId</tt>, that key entity should appear before
     *          the container in the list of passage names.</li>
     * </ul>
     * Subclasses of WorldBuilder may allow additional suffix strings not listed here.
     * <p/>
     * For each passage name listed, we will call the appropriate variant of {@link #loadEntity(String)}.
     * @param passageName name of the passage under which the JSON definition is to be found
     * @see #loadEntityType(String, String)
     */
    public void loadEntities(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonArray passageList = Json.parse(json).asArray();
            for (JsonValue v : passageList) {
                String p = v.asString();
                int idx = p.indexOf(':');
                if (idx == -1)
                    loadEntityType(p, "");
                else
                    loadEntityType(p.substring(0, idx), p.substring(idx + 1));
            }
        } catch (ParseException|UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadEntities()", ex);
        }
    }

    /**
     * Places entities into rooms based on a JSON definition given in a passage bundle.
     * A definition entry looks like this:
     * <pre>{@code
     * [
     *     [roomId1, entityId1, entityId2, ...],
     *     [roomId2, entityId1, entityId2, ...],
     *     ...
     * ]
     * }</pre>
     * All rooms and entities referenced must already have been loaded prior to calling
     * this method.
     * @param passageName
     */
    public void loadEntityPlacements(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonArray placementList = Json.parse(json).asArray();
            for (JsonValue v : placementList) {
                JsonArray arr = v.asArray();
                int n = arr.size();
                String roomId = arr.get(0).asString();
                String[] entityIds = new String[n-1];
                for (int i = 1; i < n; i++)
                    entityIds[i-1] = arr.get(i).asString();
                putEntitiesInRoom(roomId, entityIds);
            }
        } catch (ParseException|UnsupportedOperationException|IndexOutOfBoundsException ex) {
            logger.log(Level.WARNING, "JSON error, loadEntityPlacements()", ex);
        }
    }

    /**
     * Puts entities into containers based on a JSON definition given in a passage bundle.
     * A definition entry looks like this:
     * <pre>{@code
     * [
     *      [containerId1, entityId1, entityId2, ...],
     *      [containerId2, entityId1, entityId2, ...],
     *      ...
     * ]
     * }</pre>
     * @param passageName
     */
    public void loadContainerContents(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonArray contentsList = Json.parse(json).asArray();
            for (JsonValue v : contentsList) {
                JsonArray arr = v.asArray();
                int n = arr.size();
                Container c = (Container) getEntity(arr.get(0).asString());
                for (int i = 1; i < n; i++) {
                    Entity e = getEntity(arr.get(i).asString());
                    if (e != null && !c.contents.contains(e))
                        c.contents.add(e);
                }
            }
        } catch (ParseException | UnsupportedOperationException | IndexOutOfBoundsException ex) {
            logger.log(Level.WARNING, "JSON error, loadContainerContents()", ex);
        } catch (ClassCastException ex) {
            logger.log(Level.WARNING, "Invalid containerId, loadContainerContents()", ex);
        }
    }

    /**
     * Loads a room-connection defintion from JSON in a bundle passage, and connects the rooms
     * given in the defintion. A defintion entry looks like this:
     * <pre>{@code
     * [
     *    [roomId1, pos1, roomId2, pos2],
     *    [roomId1, pos1, roomId2],
     *    [doorId, roomId1, pos1, roomId2, pos2],
     *    ...
     * ]
     * }</pre>
     * The different lengths of arrays correspond to {@link #connectRooms(String, int, String, int)},
     * {@link #connectRoomsOneWay(String, int, String)}, and
     * {@link #connectRoomsWithDoor(String, String, int, String, int)},
     * respectively.
     * <p/>
     * The "pos" parameters take one of these string values:
     * <blockquote><tt>
     *      "NW", "N", "NE", "X1",<br/>
     *      "W", "MID", "E", "X2",<br/>
     *      "SW", "S", "SE", "X3"
     * </blockquote></tt>
     * No real input validation is performed, so write your JSON correctly!
     * <p/>
     * All rooms referenced in the connection list must have already been loaded prior to
     * calling this method.
     * @param passageName name of the passage under which the JSON definition is to be found
     */
    public void loadRoomConnections(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonArray connectionList = Json.parse(json).asArray();
            for (JsonValue v : connectionList) {
                JsonArray arr = v.asArray();
                switch (arr.size()) {
                case 4:
                    String roomId1 = arr.get(0).asString();
                    int pos1 = UIConstants.buttonTextToPosition(arr.get(1).asString());
                    String roomId2 = arr.get(2).asString();
                    int pos2 = UIConstants.buttonTextToPosition(arr.get(3).asString());
                    connectRooms(roomId1, pos1, roomId2, pos2);
                    break;
                case 3:
                    roomId1 = arr.get(0).asString();
                    pos1 = UIConstants.buttonTextToPosition(arr.get(1).asString());
                    roomId2 = arr.get(2).asString();
                    connectRoomsOneWay(roomId1, pos1, roomId2);
                    break;
                case 5:
                    String doorId = arr.get(0).asString();
                    roomId1 = arr.get(1).asString();
                    pos1 = UIConstants.buttonTextToPosition(arr.get(2).asString());
                    roomId2 = arr.get(3).asString();
                    pos2 = UIConstants.buttonTextToPosition(arr.get(4).asString());
                    connectRoomsWithDoor(doorId, roomId1, pos1, roomId2, pos2);
                    break;
                }
            }
        } catch (ParseException|UnsupportedOperationException|IndexOutOfBoundsException ex) {
            logger.log(Level.WARNING, "JSON error, loadRoomConnections()", ex);
        }
    }

    /**
     * Loads the initial player room and inventory list from a JSON definition given in a bundle passage.
     * The definition looks like this:
     * <pre>{@code
     * {
     *     "currentRoom" : roomId,
     *     "inventory" : [entityId1, entityId2, ...],
     *     "worn" : [entityId1, entityId2, ...],
     *     "equipped" : [entityId1, entityId2, ...]
     * }
     * }</pre>
     * <tt>currentRoom</tt> is mandatory, the rest are optional.
     * @param passageName name of the passage under which the JSON definition is to be found
     */
    public void loadPlayerState(String passageName) {
        String json = bundle.getPassage(passageName);
        try {
            JsonObject o = Json.parse(json).asObject();
            Player player = worldState.player;
            player.currentRoom = getRoom(o.get("currentRoom").asString());
            final String[] listKeys = {"inventory", "worn", "equipped"};
            final List<List<Entity>> playerLists = new ArrayList<>(3);
            playerLists.add(player.inventory);
            playerLists.add(player.worn);
            playerLists.add(player.equipped);
            for (int i = 0; i < listKeys.length; i++) {
                JsonValue v = o.get(listKeys[i]);
                if (v != null) {
                    JsonArray arr = v.asArray();
                    List<Entity> l = playerLists.get(i);
                    for (JsonValue itemVal : arr.values())
                        l.add(getEntity(itemVal.asString()));
                }
            }
            // We don't need to fix up the consistency of these lists (for instance, make sure that
            // all worn items are in inventory) or set the rooms of the inventory entities because
            // when the game starts, GameManager#ensurePlayerInventoryConsistent() will do this.
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, loadPlayerState()", ex);
        }
    }

    // These methods are meant to be overridden in subclasses to provide any additional
    // entity and room types, and attribute values that are needed.

    /**
     * Return the integer Entity attribute value that corresponds to a given string.
     * For instance, "takeable" corresponds to {@link Attributes#TAKEABLE}.
     * <p/>
     * Subclasses of WorldBuilder should override this method (chaining up to the superclass implementation)
     * to provide new entity attributes.
     * @param attrStr string representation of an entity attribute
     * @return attribute value, or -1 if the string has no corresponding attribute
     */
    protected int getEntityAttributeVal(String attrStr) {
        return Attributes.stringToEntityAttribute(attrStr);
    }

    /**
     * Return the integer Room attribute value that corresponds to a given string.
     * For instance, "visited" corresponds to {@link Attributes#VISITED}.
     * <p/>
     * Subclasses of WorldBuilder should override this method (chaining up to the superclass implementation)
     * to provide new room attributes.
     * @param attrStr string representation of a room attribute
     * @return attribute value, or -1 if the string has no corresponding attribute
     */
    protected int getRoomAttributeVal(String attrStr) {
        return Attributes.stringToRoomAttribute(attrStr);
    }

    /**
     * Called by {@link #loadEntities} to load a BaseEntity or subclass from a passage.
     * <p/>
     * Subclasses of WorldBuilder should override this method (chaining up to the superclass implementation)
     * to provide new entity types.
     * @param passageName name of the passage under which the JSON definition is to be found
     * @param typeStr a string indicating what type of entity to load (ex. "door").
     */
    protected void loadEntityType(String passageName, String typeStr) {
        switch (typeStr.toLowerCase()) {
        case "door":
            loadDoor(passageName);
            break;
        case "talking":
            loadTalkingEntity(passageName);
            break;
        case "container":
            loadContainer(passageName);
            break;
        default:
            loadEntity(passageName);
            break;
        }
    }

    /**
     * Called by {@link #loadRooms} to load a BaseRoom or subclass from a passage.
     * <p/>
     * Subclasses of WorldBuilder should override this method (chaining up to the superclass implementation)
     * to provide new room types.
     * @param passageName name of the passage under which the JSON definition is to be found
     * @param typeStr a string indicating what type of room to load (ex. "dark").
     */
    protected void loadRoomType(String passageName, String typeStr) {
        switch (typeStr.toLowerCase()) {
        case "dark":
            loadDarkRoom(passageName);
            break;
        default:
            loadRoom(passageName);
            break;
        }
    }

    /**
     * If a line in the input string ends with a backslash, <tt>'\'</tt>, it will be joined
     * to the next line. In this way we can have multi-line JSON strings.
     * @param s input text
     * @return text with escaped newlines folded
     */
    protected String foldEscapedNewlines(String s) {
        if (escapedNewlinesPattern == null)
            escapedNewlinesPattern = Pattern.compile("\\\\\\n\\s*");
        return escapedNewlinesPattern.matcher(s).replaceAll("");
    }

    /**
     * Retrieves text from a JsonObject by attempting to read the value associated with a given name;
     * if the JsonObject has no member under the given name, a default passage will be read from the
     * bundle and returned.
     * @param o JsonObject
     * @param name name of the JsonObject member whose value to read
     * @param defaultPassageName the passage returned if <tt>o</tt> has no member with the given name.
     * @return text from <tt>o</tt> or the default passage text
     */
    protected String retrieveTextOrDefault(JsonObject o, String name, String defaultPassageName) {
        JsonValue v = o.get(name);
        if (v == null)
            return bundle.getPassage(defaultPassageName);
        else
            return getJsonString(v);
    }

    /**
     * Attempt to retrieve multiple strings from a JsonObject by reading the items of an array member.
     * The strings are stored in the {@code retrievedMultiStrings} field of WorldBuilder, to avoid
     * allocation on each call.
     * <p/>
     * If the object member is not an array, the retrieved strings will all be the text stored in
     * the bundle under <tt>defaultPassageName</tt>.
     * <p/>
     * If the object member <em>is</em> an array, there are three possibilities:
     * <ol>
     *     <li>The array size is 0. In this case, all retrieved strings will be the empty string.</li>
     *     <li>The array size is less than <tt>numStrings</tt>. In this case the value of the first
     *         array item will be used for all retrieved strings.</li>
     *     <li>The array size is {@code >= numStrings}. In this case the retrieved strings are taken
     *         from the respectively indexed item of the array.</li>
     * </ol>
     * @param o JsonObject
     * @param name name of the JsonObject member whose value to read as an array
     * @param numStrings the number of strings to attempt to retrieve from the array
     *                  and store in <tt>retrievedMultiStrings</tt>
     * @param defaultPassageName the passage whose text to use if <o>o</o> has no member under <tt>name</tt>,
     *                  or if the value is not an array.
     */
    protected void retrieveMultiTextOrDefault(JsonObject o, String name, int numStrings, String defaultPassageName) {
        if (retrievedMultiStrings == null || retrievedMultiStrings.length < numStrings)
            retrievedMultiStrings = new String[numStrings];
        else
            Arrays.fill(retrievedMultiStrings, null);
        JsonValue v = o.get(name);
        if (v == null || !v.isArray()) {
            Arrays.fill(retrievedMultiStrings, 0, numStrings, bundle.getPassage(defaultPassageName));
        } else {
            JsonArray arr = v.asArray();
            if (arr.size() == 0) {
                Arrays.fill(retrievedMultiStrings, 0, numStrings, "");
            } else if (arr.size() < numStrings) {
                Arrays.fill(retrievedMultiStrings, 0, numStrings, getJsonString(arr.get(0)));
            } else {
                for (int i = 0; i < numStrings; i++)
                    retrievedMultiStrings[i] = getJsonString(arr.get(i));
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
