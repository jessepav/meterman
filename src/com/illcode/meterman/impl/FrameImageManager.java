package com.illcode.meterman.impl;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.ui.UIConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman.Utils.logger;

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

    public void setDefaultImageName(String defaultImageName) {
        this.defaultImageName = defaultImageName;
    }

    public void setRoomImage(String roomId, String imageName) {
        roomImageMap.put(roomId, imageName);
    }

    public String getRoomImage(String roomId) {
        String name = roomImageMap.get(roomId);
        return name != null ? name : defaultImageName;
    }

    public void clear() {
        roomImageMap.clear();
    }

    public Map<String,String> getRoomImageMap() {
        return roomImageMap;
    }

    public void setRoomImageMap(Map<String,String> roomImageMap) {
        this.roomImageMap = roomImageMap;
    }

    /**
     * Loads roomId->imageName mappings from JSON data of this form:
     * <pre>{@code
     * {
     *     "default" : "defaultImageName",
     *     "roomId1" : "imageName1",
     *     "roomId2" : "imageName2",
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
                String roomId = m.getName();
                String imageName = m.getValue().asString();
                if (roomId.equals("default"))
                    defaultImageName = imageName;
                else
                    roomImageMap.put(roomId, imageName);
            }
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, FrameImageManager.loadFromJson()", ex);
        }

    }

    public boolean playerMove(Room from, Room to, boolean beforeMove) {
        if (beforeMove == false) {
            String imageName;
            if (to instanceof BaseRoom)
                imageName = getRoomImage(((BaseRoom) to).id);
            else
                imageName = defaultImageName;
            Meterman.ui.setFrameImage(imageName);
        }
        return false;
    }
}
