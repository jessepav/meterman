package com.illcode.meterman.event;

import com.illcode.meterman.Entity;

import java.util.List;

/**
 * An EntityActionsProcessor is notified when the list of entity actions to display is being
 * generated. This allows listeners to add additional actions to the normal action
 * list, to be handled by an accompanying {@link GameActionListener}.
 */
public interface EntityActionsProcessor
{
    /**
     * Called when an entity's action list is being generated.
     * @param e entity
     * @param actions the mutable list of actions that should be shown in the UI, It will be passed along to
     *       any other registered {@code EntityActionsProcessor}S before being displayed.
     */
    void processEntityActions(Entity e, List<String> actions);
}
