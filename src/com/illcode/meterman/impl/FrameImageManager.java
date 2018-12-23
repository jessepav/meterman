package com.illcode.meterman.impl;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.ui.UIConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

/**
 * FrameImageManager maintains a mapping from {@link BaseRoom#id BaseRoom IDs} to image names,
 * and when the player moves rooms it will set the UI frame image accordingly.
 * <p/>
 * Particularly, with {@link #loadFromJson}, the entire setup of setting frame images can be
 * kept in one JSON object in a text bundle.
 */
public class FrameImageManager implements PlayerMovementListener
{
    private static final String FRAME_IMAGE_MANAGER_KEY = "com.illcode.meterman.impl.FrameImageManager";

    private Map<String,String> roomImageMap;
    private String defaultImageName;

    public FrameImageManager() {
    }

    public void init() {
        roomImageMap = new HashMap<>();
        defaultImageName = UIConstants.DEFAULT_FRAME_IMAGE;
    }

    public void saveTo(Map<String,Object> worldData) {
        worldData.put(FRAME_IMAGE_MANAGER_KEY, this);
    }

    public static FrameImageManager retrieveFrom(Map<String,Object> worldData) {
        return (FrameImageManager) worldData.get(FRAME_IMAGE_MANAGER_KEY);
    }

    public void register() {
        Meterman.gm.addPlayerMovementListener(this);
    }

    public void deregister() {
        Meterman.gm.removePlayerMovementListener(this);
    }


    /**
     * Set the image name associated with a given BaseRoom ID.
     * @param roomId BaseRoom id. If equal to <tt>"default"</tt>, set the default image name that will be
     *          used when the player moves to a room that doesn't have an explicitly entry in our map.
     * @param imageName image name. If equal to <tt>"default"</tt>, the value of {@link UIConstants#DEFAULT_FRAME_IMAGE}
     *          will be used.
     */
    public void setRoomImageName(String roomId, String imageName) {
        if (imageName.equals("default"))
            imageName = UIConstants.DEFAULT_FRAME_IMAGE;
        if (roomId.equals("default"))
            defaultImageName = imageName;
        else
            roomImageMap.put(roomId, imageName);
    }

    /** Get the image name associated with a given room ID. If the room doesn't have an entry
     *  in the FrameImageManager's map, it will return the <tt>"default"</tt> image name. */
    public String getRoomImageName(String roomId) {
        String name = roomImageMap.get(roomId);
        return name != null ? name : defaultImageName;
    }

    /** Remove <tt>roomId</tt> from our image map. */
    public void removeRoomImageName(String roomId) {
        roomImageMap.remove(roomId);
    }

    /** Clear all {@code roomId->imageName} entries. The default image name is left unchanged. */
    public void clear() {
        roomImageMap.clear();
    }

    /** Return the {@code roomId->imageName} map being used. */
    public Map<String,String> getRoomImageMap() {
        return roomImageMap;
    }

    /** Set the {@code roomId->imageName} map used by this manager. */
    public void setRoomImageMap(Map<String,String> roomImageMap) {
        this.roomImageMap = roomImageMap;
    }

    /**
     * Loads {@code roomId->imageName} mappings from JSON data of this form:
     * <pre>{@code
     * {
     *     "imageName1" : "default",
     *     "imageName2" : "roomId1",
     *     "imageName3" : ["roomId2", "roomId3", ...]
     *     ...
     * }
     * }</pre>
     * <tt>default</tt> is a special "room ID" that is used if the ID of the current room is
     * not specified explicitly in the mapping.
     * <p/>
     * This method replaces any mappings currently stored by the FrameImageManager.
     * @param json JSON text
     */
    public void loadFromJson(String json) {
        roomImageMap.clear();
        try {
            JsonObject o = Json.parse(json).asObject();
            for (JsonObject.Member m : o) {
                String imageName = m.getName();
                JsonValue roomIdVal = m.getValue();
                if (roomIdVal.isString()) {
                    setRoomImageName(roomIdVal.asString(), imageName);
                } else if (roomIdVal.isArray()) {
                    for (JsonValue v : roomIdVal.asArray())
                        setRoomImageName(v.asString(), imageName);
                }
            }
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, FrameImageManager.loadFromJson()", ex);
        }
    }

    /** Implement PlayerMovementListener to change the frame image as the player moves rooms. */
    public boolean playerMove(Room from, Room to, boolean beforeMove) {
        if (beforeMove == false) {
            String imageName;
            if (to instanceof BaseRoom)
                imageName = getRoomImageName(((BaseRoom) to).id);
            else
                imageName = defaultImageName;
            Meterman.ui.setFrameImage(imageName);
        }
        return false;
    }
}
