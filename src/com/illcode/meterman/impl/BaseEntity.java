package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

import java.util.*;

public class BaseEntity implements Entity
{
    public String name;
    public String listName;
    public String description;
    public BitSet attributes;
    public Room room;
    public Map<String,Object> properties;

    private EntityDelegate delegate;

    public BaseEntity() {
    }

    public void init() {
        name = "(name)";
        listName = "(list name)";
        description = "(description)";
        attributes = new BitSet(64);
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

    public String getListName() {
        return listName;
    }

    public String getDescription() {
        return description;
    }

    public void lookInRoom() {
        if (delegate != null)
            delegate.lookInRoom(this);
    }

    public void enterScope() {
        if (delegate != null)
            delegate.enterScope(this);
    }

    public void exitingScope() {
        if (delegate != null)
            delegate.exitingScope(this);
    }

    public void taken() {
        if (delegate != null)
            delegate.taken(this);
    }

    public void dropped() {
        if (delegate != null)
            delegate.dropped(this);
    }

    public void selected() {
        if (delegate != null)
            delegate.selected(this);
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        room = room;
    }

    public List<String> getActions() {
        if (delegate != null)
            return delegate.getActions(this);
        else
            return Collections.emptyList();
    }

    public boolean processAction(String action) {
        if (delegate != null)
            return delegate.processAction(this, action);
        else
            return false;
    }

    public Map<String,Object> getProperties() {
        return properties;
    }

    /**
     * Set an {@link EntityDelegate} to proxy certain method calls for this BaseEntity.
     * @param delegate delegate to set, or null to remove proxying
     */
    public void setDelegate(EntityDelegate delegate) {
        this.delegate = delegate;
    }
}
