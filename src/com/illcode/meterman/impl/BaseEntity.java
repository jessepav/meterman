package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.ui.MetermanUI;

import java.util.*;

/**
 * An Entity implementation that supports various standard features, and can
 * be used as a starting point for more game-specific subclasses.
 */
public class BaseEntity implements Entity
{
    /**
     * An ID for this particular entity, which can be used to uniquely identify it.
     */
    public String id;

    public String name;
    public String indefiniteArticle;
    public String listName;
    public String description;
    public BitSet attributes;
    public Room room;
    public Map<String,Object> properties;

    /** If not null, it will be set as the {@link MetermanUI#setEntityImage(String)} when this
     *  BaseEntity is selected() */
    public String imageName;

    protected EntityDelegate delegate;

    public BaseEntity() {
    }

    public void init() {
        id = "(id)";
        name = "(name)";
        indefiniteArticle = null;
        listName = null;
        description = "(description)";
        attributes = new BitSet(64);
        properties = new HashMap<>();
        imageName = MetermanUI.NO_IMAGE;
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

    public void setAttribute(int attribute, boolean val) {
        attributes.set(attribute, val);
    }

    public void clearAllAttributes() {
        attributes.clear();
    }

    public String getName() {
        return name;
    }

    public String getIndefiniteArticle() {
        return indefiniteArticle;
    }

    public String getListName() {
        return listName != null ? listName : name;
    }

    public String getDescription() {
        if (delegate != null)
            return delegate.getDescription(this);
        else
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

    public boolean selected() {
        if (delegate != null)
            return delegate.selected(this);
        else
            return selectedImpl();
    }

    // For subclasses and delegates
    protected boolean selectedImpl() {
        if (imageName != null)
            Meterman.ui.setEntityImage(imageName);
        return false;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    public String replaceParserMessage(String action) {
        if (delegate != null)
            return delegate.replaceParserMessage(this, action);
        else
            return null;
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

    public String toString() {
        return getListName();
    }
}
