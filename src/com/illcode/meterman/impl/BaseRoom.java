package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.ui.UIConstants;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class BaseRoom implements Room
{
    public String id;
    public String name;
    public String exitName;
    public String description;
    public BitSet attributes;
    public String[] exitIds;
    public String[] exitLabels;
    public List<Entity> entities;

    public BaseRoom() {
        id = "(id)";
        name = "(name)";
        exitName = "(exit name)";
        description = "(description)";
        attributes = new BitSet(64);
        exitIds = new String[UIConstants.NUM_EXIT_BUTTONS];
        exitLabels = new String[UIConstants.NUM_EXIT_BUTTONS];
        entities = new LinkedList<>();
    }

    public String getId() {
        return id;
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

    public String getExitId(int direction) {
        return exitIds[direction];
    }

    public String getExitLabel(int direction) {
        if (exitLabels[direction] != null)
            return exitLabels[direction];
        else if (exitIds[direction] != null)
            return Meterman.gm.getRoom(exitIds[direction]).getExitName();
        return null;
    }

    public boolean attemptExit(int direction) {
        return true;
    }

    public List<Entity> getRoomEntities() {
        return entities;
    }

    public void roomEntered() {
        // empty
    }

    public void roomExiting() {
        // empty
    }
}
