package com.illcode.meterman.impl;

import com.illcode.meterman.Room;

import java.util.List;

public interface EntityDelegate
{
    void lookInRoom(BaseEntity e);

    void enterScope(BaseEntity e);

    void exitingScope(BaseEntity e);

    void taken(BaseEntity e);

    void dropped(BaseEntity e);

    void selected(BaseEntity e);

    List<String> getActions(BaseEntity e);

    boolean processAction(BaseEntity e, String action);
}
