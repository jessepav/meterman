package com.illcode.meterman.event;

import com.illcode.meterman.Entity;
import com.illcode.meterman.GameManager;

/**
 * A GameActionListener is notified before and after the player chooses an action.
 * @see GameManager#entityActionSelected(String)
 */
public interface GameActionListener
{
    /**
     * Called when an action is sent to the selected entity. It may be called twice per action,
     * once before the action has actually been processed, and once after, if not interrupted
     * by another GameActionListener or {@link Entity#processAction(String)}.
     * @param action action name
     * @param e selected entity
     * @param beforeAction true if the method is being called before the action has reached the
     *                     entity; false if it is called after
     * @return true to indicate that this listener processed the action, and to prevent further
     *         processing; false to continue the processing chain.
     */
    boolean processAction(String action, Entity e, boolean beforeAction);

    /**
     * Called at the end of the action-processing chain regardless if the chain was interrupted
     * (i.e. handled) by a listener or the entity.
     * @param action action name
     * @param e selected entity
     * @param actionHandled true if the action was processed (by a listener or the entity returning
     * @return true to suppress the normal "Nothing much happened" message if actionHandled is false.
     */
    boolean postAction(String action, Entity e, boolean actionHandled);
}
