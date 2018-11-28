package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

import java.util.List;

/**
 * An interface that allows you to proxy calls to the methods of a {@link BaseRoom}
 * that have to do with world activity, as opposed to simple properties.
 */
public interface EntityDelegate
{
    /** @see Entity#lookInRoom() */
    void lookInRoom(BaseEntity e);

    /** @see Entity#enterScope() */
    void enterScope(BaseEntity e);

    /** @see Entity#exitingScope()  */
    void exitingScope(BaseEntity e);

    /** @see Entity#taken()  */
    void taken(BaseEntity e);

    /** @see Entity#dropped()  */
    void dropped(BaseEntity e);

    /** @see Entity#selected()  */
    void selected(BaseEntity e);

    /** @see Entity#getActions()  */
    List<String> getActions(BaseEntity e);

    /** @see Entity#processAction(String)  */
    boolean processAction(BaseEntity e, String action);
}
