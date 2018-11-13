package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;

import java.util.*;

public class BaseEntity implements Entity
{
    public String id;
    public String name;
    public String listName;
    public String description;
    public BitSet attributes;
    public String roomId;
    public Map<String,Object> data;

    public BaseEntity() {
    }

    public void init() {
        id = "(id)";
        name = "(name)";
        listName = "(list name)";
        description = "(description)";
        attributes = new BitSet(64);
        roomId = "(room ID)";
        data = new HashMap<>();
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

    public void taken() {
        // empty
    }

    public void dropped() {
        // empty
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String id) {
        roomId = id;
    }

    public List<String> getActions() {
        return Collections.emptyList();
    }

    public boolean processAction(String action) {
        return false;
    }

    public Map<String,Object> getData() {
        return data;
    }
}
