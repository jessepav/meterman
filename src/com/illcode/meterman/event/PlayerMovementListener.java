package com.illcode.meterman.event;

import com.illcode.meterman.GameManager;
import com.illcode.meterman.Room;

/**
 * A PlayerMovementListener is notified before and after the player moves rooms
 */
public interface PlayerMovementListener
{
    /**
     * Called when a player moves rooms.
     * @param from the room the player is moving, or has moved, from
     * @param to the room the player is moving, or has moved, to
     * @param beforeMove true if this method is being called before the player moves
     * @return true if the further processing of the player move command should be blocked;
     * false to let processing continue as usual.
     * @see GameManager#addBeforePlayerMovementListener(PlayerMovementListener)
     * @see GameManager#addAfterPlayerMovementListener(PlayerMovementListener)
     */
    boolean playerMove(Room from, Room to, boolean beforeMove);
}
