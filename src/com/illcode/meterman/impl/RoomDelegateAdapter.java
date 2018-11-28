package com.illcode.meterman.impl;

/**
 * An empty RoomDelegate implementation that can be subclassed to override
 * specific methods.
 */
public class RoomDelegateAdapter implements RoomDelegate
{
    public String getDescription(BaseRoom r) {
        return null;
    }

    public void entered(BaseRoom r) {
        // empty
    }

    public void exiting(BaseRoom r) {
        // empty
    }
}
