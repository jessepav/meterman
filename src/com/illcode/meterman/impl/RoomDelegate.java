package com.illcode.meterman.impl;

import com.illcode.meterman.Room;

/**
 * An interface that allows you to proxy calls to certain methods of a {@link BaseRoom}.
 * @see RoomDelegateAdapter
 */
public interface RoomDelegate
{
    /**
     * Calls to {@link BaseRoom#getDescription()} will be forwarded to this method, with
     * the proviso that if this method returns null, {@code BaseRoom.getDescription()} will
     * return the usual, simple description property.
     * @param r forwarding BaseRoom
     * @return description text, or null
     */
    String getDescription(BaseRoom r);

    /** @see Room#entered() */
    void entered(BaseRoom r);

    /** @see Room#exiting() */
    void exiting(BaseRoom r);
}
