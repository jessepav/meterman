package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Game;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.event.EntityActionsProcessor;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.ui.UIConstants;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.Attributes.*;
import static com.illcode.meterman.impl.BasicActions.*;

import java.util.List;

/**
 * A manager (global listener for various game events) that handles basic
 * world interactions, namely
 * <ul>
 *     <li>Taking and dropping items.</li>
 *     <li>Wearing and taking off wearables.</li>
 *     <li>Equipping and unequipping equippables.</li>
 * </ul>
 */
public class BasicWorldManager implements GameActionListener, EntityActionsProcessor, TurnListener
{
    public static final String BASIC_WORLD_MANAGER_KEY = "com.illcode.meterman.impl.BasicWorldManager";

    public BasicWorldManager() {
    }

    /**
     * Registers the BasicWorldManager with the GameManager, and saves the instance in the world-data.
     * <p/>
     * This should be called in {@link Game#start(boolean)} when a new game is started.
     */
    public void register() {
        gm.addDefaultGameActionListener(this);
        gm.addEntityActionsProcessor(this);
        gm.addTurnListener(this);
        gm.getWorldData().put(BASIC_WORLD_MANAGER_KEY, this);
    }

    /**
     * De-registers this BasicWorldManager from the GameManager.
     * <p/>
     * It does <em>not</em> remove the reference to the instance from the world data, so that the instance
     * be retrieved later on and re-registered without losing state.
     */
    public void deregister() {
        gm.removeDefaultGameActionListener(this);
        gm.removeEntityActionsProcessor(this);
        gm.removeTurnListener(this);
    }

    /** Retrieve the BasicWorldManager instance (saved by {@link #register()}) from the
     *  WorldState's worldData */
    public static BasicWorldManager getBasicWorldManager() {
        return (BasicWorldManager) gm.getWorldData().get(BASIC_WORLD_MANAGER_KEY);
    }

    public void processEntityActions(Entity e, List<String> actions) {
        if (e.checkAttribute(TAKEABLE)) {
            if (gm.isInInventory(e))
                actions.add(DROP_ACTION);
            else
                actions.add(TAKE_ACTION);
        }
        if (e.checkAttribute(WEARABLE) && gm.isInInventory(e)) {
            if (gm.isWorn(e))
                actions.add(TAKEOFF_ACTION);
            else
                actions.add(WEAR_ACTION);
        }
        if (e.checkAttribute(EQUIPPABLE) && gm.isInInventory(e)) {
            if (gm.isEquipped(e))
                actions.add(UNEQUIP_ACTION);
            else
                actions.add(EQUIP_ACTION);
        }
    }

    public boolean processAction(String action, Entity e, boolean beforeAction) {
        if (action.equals(DROP_ACTION)) {
            gm.moveEntity(e, e.getRoom());
            return true;
        } else if (action.equals(TAKE_ACTION)) {
            gm.takeEntity(e);
            return true;
        } else if (action.equals(TAKEOFF_ACTION)) {
            gm.setWorn(e, false);
            return true;
        } else if (action.equals(WEAR_ACTION)) {
            gm.setWorn(e, true);
            return true;
        } else if (action.equals(UNEQUIP_ACTION)) {
            gm.setEquipped(e, false);
            return true;
        } else if (action.equals(EQUIP_ACTION)) {
            gm.setEquipped(e, true);
            return true;
        } else {
            return false;
        }
    }

    public void turn() {
        Meterman.ui.setStatusLabel(UIConstants.RIGHT_LABEL, "Turns: " + (gm.getNumTurns() + 1));
    }
}
