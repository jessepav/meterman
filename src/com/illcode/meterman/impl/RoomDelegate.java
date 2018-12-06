package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

import java.util.List;

/**
 * An interface that allows you to proxy calls to certain methods of a {@link BaseRoom}.
 * @see RoomDelegateAdapter
 */
public interface RoomDelegate
{
    /** @see Room#getDescription() */
    String getDescription(BaseRoom r);

    /**
     * Called when the player has entered the room.
     * @param r the room for which we're the delegate
     * @param fromRoom the room (possibly null) from which the player entered
     * @see Room#entered(Room)
     */
    void entered(BaseRoom r, Room fromRoom);

    /**
     * Called as the player is exiting the room (but is still there).
     * @param r the room for which we're the delegate
     * @param toRoom the room the player is attempting to move to.
     * @return true to block the player from exiting, false to allow the exit (note that
     *          the exit may fail for other reasons)
     * @see Room#exiting(Room)
     */
    boolean exiting(BaseRoom r, Room toRoom);

    /** @see Room#getRoomEntities() */
    List<Entity> getRoomEntities(BaseRoom r);
}
