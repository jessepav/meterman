package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

import java.util.List;

/**
 * An empty RoomDelegate implementation that can be subclassed to override specific methods.
 */
public class RoomDelegateAdapter implements RoomDelegate
{
    public String getDescription(BaseRoom r) {
        return r.description;
    }

    public void entered(BaseRoom r, Room fromRoom) {
        // empty
    }

    public boolean exiting(BaseRoom r, Room toRoom) {
        return false;
    }

    public List<Entity> getRoomEntities(BaseRoom r) {
        return r.entities;
    }
}
