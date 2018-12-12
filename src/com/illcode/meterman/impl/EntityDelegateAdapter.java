package com.illcode.meterman.impl;

import com.illcode.meterman.Meterman;

import java.util.Collections;
import java.util.List;

/**
 * An empty EntityDelegate implementation that can be subclassed to override
 * specific methods.
 */
public class EntityDelegateAdapter implements EntityDelegate
{
    public String getDescription(BaseEntity e) {
        return e.description;
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

    public boolean selected(BaseEntity e) {
        if (e.imageName != null)
            Meterman.ui.setEntityImage(e.imageName);
        return false;
    }

    public List<String> getActions(BaseEntity e) {
        return Collections.emptyList();
    }

    public boolean processAction(BaseEntity e, String action) {
        return false;
    }

    public String replaceParserMessage(BaseEntity e, String action) {
        return null;
    }
}
