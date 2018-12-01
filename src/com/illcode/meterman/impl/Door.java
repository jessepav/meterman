package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.ui.UIConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import static com.illcode.meterman.Meterman.gm;

/**
 * A door is a special usage entity that can connect and disconnect two rooms, depending
 * on whether it is locked.
 * <p/>
 * It maintains lists of two rooms, descriptions to be shown in each room, and messages
 * to be shown in each room depending on whether it is locked or not.
 */
public class Door extends BaseEntity
{
    public static final String DOOR_OPEN_ACTION_NAME = Utils.getActionName("Open");
    public static final String DOOR_CLOSE_ACTION_NAME = Utils.getActionName("Close");
    public static final String DOOR_UNLOCK_ACTION_NAME = Utils.getActionName("Unlock");
    public static final String DOOR_LOCK_ACTION_NAME = Utils.getActionName("Lock");

    private BaseRoom[] rooms;
    private int[] positions;
    private String[] descriptions;
    private String[] lockedMessages;
    private String[] unlockedMessages;
    private String[] noKeyMessages;
    private String[] openMessages;

    private Entity key;
    private boolean locked;
    private boolean open;
    private List<String> actions;

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
        openMessages = new String[] {"", ""};
        actions = new ArrayList<>(4);
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

    public void setOpenMessages(String msg1, String msg2) {
        openMessages[0] = msg1;
        openMessages[1] = msg2;
    }

    /**
     * Get the key needed to lock and unlock this door.
     * @return the key entity, or null if no key is required
     */
    public Entity getKey() {
        return key;
    }

    /**
     * Sets the key needed to lock and unlock this door. The key is an Entity
     * that must be in the player's inventory to perform these operations.
     * @param key key entity, or null if no key is required
     */
    public void setKey(Entity key) {
        this.key = key;
        if (key == null)
            locked = false;
    }

    /** Returns true if the door is locked. */
    public boolean isLocked() {
        return locked;
    }

    /** Set whether the door is locked. */
    public void setLocked(boolean locked) {
        this.locked = locked;
        if (locked)
            open = false;  // a locked door is necessarily closed
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (open)
            locked = false;  // you cannot have an open, locked door
    }

    public String getDescription() {
        int idx = ArrayUtils.indexOf(rooms, Meterman.gm.getCurrentRoom());
        if (idx == -1) {
            return "A door broken by the implementor.";
        } else {
            String extraMsg;
            if (open)
                extraMsg = openMessages[idx];
            else
                extraMsg = (locked ? lockedMessages[idx] : unlockedMessages[idx]);
            return descriptions[idx] + " " + extraMsg;
        }
    }

    public List<String> getActions() {
        actions.clear();
        if (key == null)
            locked = false;
        if (locked) {
            actions.add(DOOR_UNLOCK_ACTION_NAME);
        } else { // okay, we're unlocked
            if (open) {
                actions.add(DOOR_CLOSE_ACTION_NAME);
            } else { // closed but unlocked
                actions.add(DOOR_OPEN_ACTION_NAME);
                if (key != null)
                    actions.add(DOOR_LOCK_ACTION_NAME);
            }
        }
        return actions;
    }

    public boolean processAction(String action) {
        int idx = ArrayUtils.indexOf(rooms, Meterman.gm.getCurrentRoom());
        if (idx == -1)
            return false;
        if (action.equals(DOOR_LOCK_ACTION_NAME) || action.equals(DOOR_UNLOCK_ACTION_NAME)) {
            // note that in these cases we already know that key != null
            if (!Meterman.gm.isInInventory(key)) {
                Meterman.ui.appendNewline();
                Meterman.ui.appendText(noKeyMessages[idx]);
            } else {
                locked = !locked;
                gm.entityChanged(this);
            }
            return true;
        } else if (action.equals(DOOR_OPEN_ACTION_NAME) || action.equals(DOOR_CLOSE_ACTION_NAME)) {
            open = !open;
            if (open) {
                rooms[0].exits[positions[0]] = rooms[1];
                rooms[1].exits[positions[1]] = rooms[0];
            } else { // closed
                for (int i = 0; i < 2; i++) {
                    rooms[i].exits[positions[i]] = null;
                    rooms[i].exitLabels[positions[i]] = null;
                }
            }
            gm.entityChanged(this);
            Meterman.gm.roomChanged(rooms[0]);
            Meterman.gm.roomChanged(rooms[1]);
            return true;
        } else {
            return false;
        }
    }
}
