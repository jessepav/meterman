package com.illcode.meterman.event;

import com.illcode.meterman.Entity;

/**
 * A GameActionListener is notified before and after the player chooses an action.
 */
public interface GameActionListener
{
    /**
     * Called before an action is sent to the selected entity
     * @param action action name
     * @param entity selected entity, or null if this is a global action, like "Wait"
     * @return true to block the entity (and any other GameActionListener) from
     *         processing the action; false to allow further processing
     */
    boolean beforeAction(String action, Entity entity);

    /**
     * Called after an action is sent to the selected entity, and before any
     * {@link TurnListener}S are notified.
     * @param action action name
     * @param entity selected entity, or null if this is a global action, like "Wait"
     */
    void afterAction(String action, Entity entity);
}
