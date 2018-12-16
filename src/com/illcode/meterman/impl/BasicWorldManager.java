package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Game;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.event.EntityActionsProcessor;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.ParserMessageProcessor;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.UIConstants;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.GameUtils.indefName;
import static com.illcode.meterman.GameUtils.defName;
import static com.illcode.meterman.Attributes.*;
import static com.illcode.meterman.impl.BasicActions.*;

import java.util.List;
import java.util.Map;

/**
 * A manager (global listener for various game events) that handles basic
 * world interactions. Namely
 * <ul>
 *     <li>Taking and dropping items.</li>
 *     <li>Wearing and taking off wearables.</li>
 *     <li>Equipping and unequipping equippables.</li>
 * </ul>
 * It also shows the number of turns passed in the {@link MetermanUI#setStatusLabel(int, String) right status label}.
 */
public class BasicWorldManager implements GameActionListener, EntityActionsProcessor, TurnListener
{
    public static final String BASIC_WORLD_MANAGER_KEY = "com.illcode.meterman.impl.BasicWorldManager";

    public BasicWorldManager() {
    }

    /** Save this instance into a world-data map. */
    public void saveTo(Map<String,Object> worldData) {
        worldData.put(BASIC_WORLD_MANAGER_KEY, this);
    }

    /** Retrieve the BasicWorldManager instance stored by {@link #saveTo(Map)} from worldData. */
    public static BasicWorldManager retrieveFrom(Map<String,Object> worldData) {
        return (BasicWorldManager) worldData.get(BASIC_WORLD_MANAGER_KEY);
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

    public void processEntityActions(Entity e, List<String> actions) {
        if (e.checkAttribute(TAKEABLE)) {
            if (gm.isInInventory(e))
                actions.add(getDropAction());
            else
                actions.add(getTakeAction());
        }
        if (e.checkAttribute(WEARABLE) && gm.isInInventory(e)) {
            if (gm.isWorn(e))
                actions.add(getTakeOffAction());
            else
                actions.add(getWearAction());
        }
        if (e.checkAttribute(EQUIPPABLE) && gm.isInInventory(e)) {
            if (gm.isEquipped(e))
                actions.add(getUnequipAction());
            else
                actions.add(getEquipAction());
        }
    }

    public boolean processAction(String action, Entity e, boolean beforeAction) {
        if (action.equals(getDropAction())) {
            gm.moveEntity(e, e.getRoom());
            return true;
        } else if (action.equals(getTakeAction())) {
            gm.takeEntity(e);
            return true;
        } else if (action.equals(getTakeOffAction())) {
            gm.setWorn(e, false);
            return true;
        } else if (action.equals(getWearAction())) {
            gm.setWorn(e, true);
            return true;
        } else if (action.equals(getUnequipAction())) {
            gm.setEquipped(e, false);
            return true;
        } else if (action.equals(getEquipAction())) {
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
