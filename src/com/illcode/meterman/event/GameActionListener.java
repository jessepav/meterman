package com.illcode.meterman.event;

import com.illcode.meterman.Entity;

/**
 * A GameActionListener is notified before and after the player chooses an action.
 */
public interface GameActionListener
{
    /**
     * Called when an action is sent to the selected entity. It may be called twice per action,
     * once before the action has actually been processed, and once after.
     * @param action action name
     * @param e selected entity
     * @param beforeAction true if the method is being called before the action has reached the
     *                     entity; false if it is called after
     * @return true to indicate that this listener processed the action, and to prevent further
     *         processing; false to continue the processing chain.
     */
    boolean processAction(String action, Entity e, boolean beforeAction);
}
