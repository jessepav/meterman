package com.illcode.meterman.impl;

import java.util.Collections;
import java.util.List;

/**
 * An empty EntityDelegate implementation that can be subclassed to override
 * specific methods.
 */
public class EntityDelegateAdapter implements EntityDelegate
{
    public String getDescription(BaseEntity e) {
        return null;
    }

    public void lookInRoom(BaseEntity e) {
        // empty
    }

    public void enterScope(BaseEntity e) {
        // empty
    }

    public void exitingScope(BaseEntity e) {
        // empty
    }

    public void taken(BaseEntity e) {
        // empty
    }

    public void dropped(BaseEntity e) {
        // empty
    }

    public void selected(BaseEntity e) {
        // empty
    }

    public List<String> getActions(BaseEntity e) {
        return Collections.emptyList();
    }

    public boolean processAction(BaseEntity e, String action) {
        return false;
    }

    public boolean suppressParserMessage(String action) {
        return false;
    }
}
