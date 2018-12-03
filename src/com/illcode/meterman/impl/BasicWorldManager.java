package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Game;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.event.EntityActionsProcessor;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.ui.UIConstants;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.Attributes.*;

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

    public static final String TAKE_ACTION_NAME = Utils.getActionName("Take");
    public static final String DROP_ACTION_NAME = Utils.getActionName("Drop");
    public static final String EQUIP_ACTION_NAME = Utils.getActionName("Equip");
    public static final String UNEQUIP_ACTION_NAME = Utils.getActionName("Unequip");
    public static final String WEAR_ACTION_NAME = Utils.getActionName("Wear");
    public static final String TAKEOFF_ACTION_NAME = Utils.getActionName("Take Off");

    public BasicWorldManager() {
    }

    /**
     * Installs the BasicWorldManager in the GameManager, and saves the instance in the world-data.
     * <p/>
     * This should be called in {@link Game#start(boolean)} when a new game is started.
     */
    public void install() {
        gm.addDefaultGameActionListener(this);
        gm.addEntityActionsProcessor(this);
        gm.addTurnListener(this);
        gm.getWorldData().put(BASIC_WORLD_MANAGER_KEY, this);
    }

    /**
     * Removes this BasicWorldManager from the GameManager and world-data.
     */
    public void remove() {
        gm.removeDefaultGameActionListener(this);
        gm.removeEntityActionsProcessor(this);
        gm.getWorldData().remove(BASIC_WORLD_MANAGER_KEY);
    }

    /** Retrieve the BasicWorldManager instance (saved by {@link #install()}) from the
     *  WorldState's worldData */
    public static BasicWorldManager getBasicWorldManager() {
        return (BasicWorldManager) gm.getWorldData().get(BASIC_WORLD_MANAGER_KEY);
    }

    public void processEntityActions(Entity e, List<String> actions) {
        if (e.checkAttribute(TAKEABLE)) {
            if (gm.isInInventory(e))
                actions.add(DROP_ACTION_NAME);
            else
                actions.add(TAKE_ACTION_NAME);
        }
        if (e.checkAttribute(WEARABLE) && gm.isInInventory(e)) {
            if (gm.isWorn(e))
                actions.add(TAKEOFF_ACTION_NAME);
            else
                actions.add(WEAR_ACTION_NAME);
        }
        if (e.checkAttribute(EQUIPPABLE) && gm.isInInventory(e)) {
            if (gm.isEquipped(e))
                actions.add(UNEQUIP_ACTION_NAME);
            else
                actions.add(EQUIP_ACTION_NAME);
        }
    }

    public boolean processAction(String action, Entity e, boolean beforeAction) {
        if (action.equals(DROP_ACTION_NAME)) {
            gm.moveEntity(e, e.getRoom());
            return true;
        } else if (action.equals(TAKE_ACTION_NAME)) {
            gm.takeEntity(e);
            return true;
        } else if (action.equals(TAKEOFF_ACTION_NAME)) {
            gm.setWorn(e, false);
            return true;
        } else if (action.equals(WEAR_ACTION_NAME)) {
            gm.setWorn(e, true);
            return true;
        } else if (action.equals(UNEQUIP_ACTION_NAME)) {
            gm.setEquipped(e, false);
            return true;
        } else if (action.equals(EQUIP_ACTION_NAME)) {
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
