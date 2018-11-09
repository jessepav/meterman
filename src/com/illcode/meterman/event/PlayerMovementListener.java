package com.illcode.meterman.event;

import com.illcode.meterman.Room;

/**
 * A PlayerMovementListener is notified before and after the player moves rooms
 */
public interface PlayerMovementListener
{
    /**
     * Called before a player moves rooms.
     * @param from the room the player is moving from (he's still in this room)
     * @param to the room the player is moving to
     * @return true if the player should be blocked from moving (and to prevent any other
     *         PlayerMovementListener from being notified); false to allow further processing
     */
    boolean playerMoving(Room from, Room to);

    /**
     * Called after a player moves rooms
     * @param from the room the player is moving from
     * @param to the room the player is moving to (he's now in this room)
     */
    void playerMoved(Room from, Room to);
}
