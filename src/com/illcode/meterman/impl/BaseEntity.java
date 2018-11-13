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

    public void enterScope() {
        // empty
    }

    public void exitingScope() {
        // empty
    }

    public void taken() {
        // empty
    }

    public void dropped() {
        // empty
    }

    public void selected() {
        // empty
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        room = room;
    }

    public List<String> getActions() {
        return Collections.emptyList();
    }

    public boolean processAction(String action) {
        return false;
    }

    public Map<String,Object> getProperties() {
        return properties;
    }
}
