package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;

/**
 * An interface to support notification when an item is put in or taken out of
 * a {@link Container}.
 */
public interface ContainerListener
{
    /**
     * Called when an entity is being added to or removed from a container. This
     * method may be called twice for every container event: the first time before
     * the item has been added or removed, and the second time after the item movement
     * has actually taken place.
     * @param c container
     * @param e entity
     * @param isAdded true if the entity is being added, false if being removed
     * @param beforeEntityMove true if method is being called before item movement has been
     *          performed, false otherwise.
     * @return true to interrupt the normal add/remove processing chain, false to allow it
     *          to proceed as normal.
     */
    boolean contentsChange(Container c, Entity e, boolean isAdded, boolean beforeEntityMove);
}
