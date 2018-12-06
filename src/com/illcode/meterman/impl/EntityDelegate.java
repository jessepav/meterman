package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;

import java.util.List;

/**
 * An interface that allows you to proxy calls to certain methods of a {@link BaseEntity}.
 * @see EntityDelegateAdapter
 */
public interface EntityDelegate
{
    /** @see Entity#getDescription()  */
    String getDescription(BaseEntity e);

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

    /** @see Entity#suppressParserMessage(String) */
    boolean suppressParserMessage(String action);
}
