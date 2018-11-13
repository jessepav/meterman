package com.illcode.meterman.event;

import com.illcode.meterman.Entity;

import java.util.List;

/**
 * An EntitySelectionListener is notified every time an entity is selected in the UI.
 * This allows listeners to, for example, add additional actions to the normal action
 * list, to be handled by an accompanying {@link GameActionListener}.
 */
public interface EntitySelectionListener
{
    /**
     * Called when an entity is selected.
     * @param e entity selected
     * @return the list of actions that should be shown in the UI. This potentially modified
     *         list is passed on to any other remaining {@code EntitySelectionListener}S.
     */
    List<String> entitySelected(Entity e);
}
