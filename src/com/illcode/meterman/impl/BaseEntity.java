package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class BaseEntity implements Entity
{
    public String id;
    public String name;
    public String listName;
    public String description;
    public BitSet attributes;
    public String roomId;

    public BaseEntity() {
        id = "(id)";
        name = "(name)";
        listName = "(list name)";
        description = "(description)";
        attributes = new BitSet(64);
        roomId = "(room ID)";
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String id) {
        roomId = id;
    }

    public List<String> getExtraActions() {
        return Collections.emptyList();
    }

    public void processAction(String action) {
        // empty
    }
}
