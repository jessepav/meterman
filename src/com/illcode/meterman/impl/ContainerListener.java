package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;

/**
 * An interface to support notification when an item is being put in or taken out of
 * a {@link Container}.
 */
public interface ContainerListener
{
    /**
     * Called when an entity is being added to or removed from a container.
     * @param c container
     * @param e entity
     * @param isAdded true if the entity is being added, false if being removed
     * @return true to block this addition or removal, false to allow it to proceed
     */
    boolean contentsChanging(Container c, Entity e, boolean isAdded);
}
