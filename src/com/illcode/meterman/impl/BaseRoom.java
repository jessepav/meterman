package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;
import com.illcode.meterman.ui.UIConstants;

import java.util.*;

public class BaseRoom implements Room
{
    public String name;
    public String exitName;
    public String description;
    public BitSet attributes;
    public Room[] exits;
    public String[] exitLabels;
    public List<Entity> entities;
    public Map<String,Object> properties;

    public BaseRoom() {
    }

    public void init() {
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
        return null;
    }

    public void setExit(int direction, Room room) {
        exits[direction] = room;
    }

    public void setExitLabel(int direction, String label) {
        exitLabels[direction] = label;
    }

    public List<Entity> getRoomEntities() {
        return entities;
    }

    public void entered() {
        // empty
    }

    public void exiting() {
        // empty
    }

    public Map<String,Object> getProperties() {
        return properties;
    }
}
