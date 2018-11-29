package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.ui.UIConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A door is a special usage entity that can connect and disconnect two rooms, depending
 * on whether it is locked.
 * <p/>
 * It maintains lists of two rooms, descriptions to be shown in each room, and messages
 * to be shown in each room depending on whether it is locked or not.
 */
public class Door extends BaseEntity
{
    private BaseRoom[] rooms;
    private int[] positions;
    private String[] descriptions;
    private String[] lockedMessages;
    private String[] unlockedMessages;
    private String[] noKeyMessages;

    private Entity key;
    private boolean locked;

    public Door() {
    }

    public void init() {
        super.init();
        rooms = new BaseRoom[2];
        positions = new int[] {-1, -1};
        descriptions = new String[] {"", ""};
        lockedMessages = new String[] {"", ""};
        unlockedMessages = new String[] {"", ""};
        noKeyMessages = new String[] {"", ""};
    }

    /** Set the two rooms connected by this door. */
    public void setRooms(BaseRoom room1, BaseRoom room2) {
        rooms[0] = room1;
        rooms[1] = room2;
    }

    /** Get the first or second room {@code (roomNo == 0 or 1)}*/
    public BaseRoom getRoom(int roomNo) {
        return rooms[roomNo];
    }

    /**
     * Sets the positions (ex. {@link UIConstants#NW_BUTTON}) in the first and second room to be connected when the
     * door is unlocked.
     * @param pos1 exit position in first room
     * @param pos2 exit position in second room
     */
    public void setPositions(int pos1, int pos2) {
        positions[0] = pos1;
        positions[1] = pos2;
    }

    /** Sets the description of the door to be shown in the first and second room. */
    public void setDescriptions(String description1, String description2) {
        descriptions[0] = description1;
        descriptions[1] = description2;
    }

    /** Sets the messages shown in the first and second room when the door is locked.
     *  These messages are shown in addition to the description. */
    public void setLockedMessages(String msg1, String msg2) {
        lockedMessages[0] = msg1;
        lockedMessages[1] = msg2;
    }

    /** Sets the messages shown in the first and second room when the door is unlocked.
     *  These messages are shown in addition to the description. */
    public void setUnlockedMessages(String msg1, String msg2) {
        unlockedMessages[0] = msg1;
        unlockedMessages[1] = msg2;
    }

    /** Sets the message shown when the player attempts to lock or unlock the door without
     *  holding the key, in room 1 and 2 respectively. */
    public void setNoKeyMessages(String msg1, String msg2) {
        noKeyMessages[0] = msg1;
        noKeyMessages[1] = msg2;
    }

    /**
     * Get the key needed to lock and unlock this door.
     * @return the key entity
     */
    public Entity getKey() {
        return key;
    }

    /**
     * Sets the key needed to lock and unlock this door. The key is an Entity
     * that must be in the player's inventory to perform these operations.
     * @param key key entity
     */
    public void setKey(Entity key) {
        this.key = key;
    }

    /** Returns true if the door is locked. */
    public boolean isLocked() {
        return locked;
    }

    /** Set whether the door is locked. */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getDescription() {
        int idx = ArrayUtils.indexOf(rooms, Meterman.gm.getCurrentRoom());
        if (idx == -1)
            return "A door broken by the implementor.";
        else
            return descriptions[idx] + " " + (locked ? lockedMessages[idx] : unlockedMessages[idx]);
    }

    public List<String> getActions() {
        return Arrays.asList(locked ? "Unlock" : "Lock");
    }

    public boolean processAction(String action) {
        if (!action.equals("Lock") && !action.equals("Unlock"))
            return false;
        int idx = ArrayUtils.indexOf(rooms, Meterman.gm.getCurrentRoom());
        if (idx == -1)
            return false;
        // From here on, we're in charge and will return true
        if (key == null || !Meterman.gm.isEntityInInventory(key)) {
            Meterman.ui.appendNewline();
            Meterman.ui.appendText(noKeyMessages[idx]);
        } else {
            locked = !locked;
            if (!locked) {
                rooms[0].exits[positions[0]] = rooms[1];
                rooms[1].exits[positions[1]] = rooms[0];
            } else {
                for (int i = 0; i < 2; i++) {
                    rooms[i].exits[positions[i]] = null;
                    rooms[i].exitLabels[positions[i]] = null;
                }
            }
            Meterman.gm.roomChanged(rooms[0]);
            Meterman.gm.roomChanged(rooms[1]);
        }
        return true;
    }
}
