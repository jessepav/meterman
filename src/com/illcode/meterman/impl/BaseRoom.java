package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;
import com.illcode.meterman.ui.UIConstants;

import java.util.*;

/**
 * A Room implementation that supports various standard features, and can
 * be used as a starting point for more game-specific implementations.
 */
public class BaseRoom implements Room
{
    /**
     * An ID for this particular room, which can be used to uniquely identify it.
     */
    public String id;

    public String name;
    public String exitName;
    public String description;
    public BitSet attributes;
    public Room[] exits;
    public String[] exitLabels;
    public LinkedList<Entity> entities;
    public HashMap<String,Object> properties;

    private RoomDelegate delegate;

    public BaseRoom() {
    }

    public void init() {
        id = "(id)";
        name = "(name)";
        exitName = "(exit name)";
        description = "(description)";
        attributes = new BitSet(64);
        exits = new Room[UIConstants.NUM_EXIT_BUTTONS];
        exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
        entities = new LinkedList<>();
        properties = new HashMap<>();
    }

    public boolean checkAttribute(int attribute) {
        return attributes.get(attribute);
    }

    public void clearAttribute(int attribute) {
        attributes.clear(attribute);
    }

    public void setAttribute(int attribute) {
        attributes.set(attribute);
    }

    public void clearAllAttributes() {
        attributes.clear();
    }

    public String getName() {
        return name;
    }

    public String getExitName() {
        return exitName;
    }

    public String getDescription() {
        if (delegate != null) {
            String s = delegate.getDescription(this);
            if (s != null)
                return s;
        }
        return description;
    }

    public Room getExit(int direction) {
        return exits[direction];
    }

    public String getExitLabel(int direction) {
        if (exitLabels[direction] != null)
            return exitLabels[direction];
        else if (exits[direction] != null)
            return exits[direction].getExitName();
        else
            return null;
    }

    public List<Entity> getRoomEntities() {
        return entities;
    }

    public void entered() {
        if (delegate != null)
            delegate.entered(this);
    }

    public void exiting() {
        if (delegate != null)
            delegate.exiting(this);
    }

    public Map<String,Object> getProperties() {
        return properties;
    }

    /**
     * Set a {@link RoomDelegate} to proxy certain method calls for this BaseRoom.
     * @param delegate delegate to set, or null to remove proxying
     */
    public void setDelegate(RoomDelegate delegate) {
        this.delegate = delegate;
    }
}
