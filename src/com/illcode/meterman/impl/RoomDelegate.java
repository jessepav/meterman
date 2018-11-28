package com.illcode.meterman.impl;

import com.illcode.meterman.Room;

/**
 * An interface that allows you to proxy calls to the methods of a {@link BaseRoom}
 * that have to do with world activity, as opposed to simple properties.
 */
public interface RoomDelegate
{
    /** @see Room#entered() */
    void entered(BaseRoom r);

    /** @see Room#exiting() */
    void exiting(BaseRoom r);
}
