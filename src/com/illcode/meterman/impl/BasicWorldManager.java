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
import static com.illcode.meterman.Meterman.ui;
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
    public static final int DEFAULT_MAX_INVENTORY = 1024;

    private boolean updateStatusBar;
    private int maxInventoryItems;

    public BasicWorldManager() {
    }

    public void init() {
        updateStatusBar = true;
        maxInventoryItems = DEFAULT_MAX_INVENTORY;
    }

    /** Returns the maximum # of inventory items the player can carry. */
    public int getMaxInventoryItems() {
        return maxInventoryItems;
    }

    /** Sets the maximum # of inventory items the player can carry. */
    public void setMaxInventoryItems(int maxInventoryItems) {
        this.maxInventoryItems = maxInventoryItems;
    }

    /**
     * Sets whether the BasicWorldManager should update the status bar each turn.
     * <p/><br/>
     * <b>Status Bar</b><br/>
     * <table border="1">
     *     <tr><th>Left</th><th>Center</th><th>Right</th></tr>
     *     <tr><td>(unchanged)</td><td>(unchanged)</td><td>Turns: &lt;# of turns&gt;</td></tr>
     * </table>
     * @param updateStatusBar true to enable status bar updates.
     */
    public void setUpdateStatusBar(boolean updateStatusBar) {
        this.updateStatusBar = updateStatusBar;
    }

    /** Save this instance into a world-data map. */
    public void saveTo(Map<String,Object> worldData) {
        worldData.put(BASIC_WORLD_MANAGER_KEY, this);
    }

    /** Retrieve the BasicWorldManager instance stored by {@link #saveTo(Map)} from worldData. */
    public static BasicWorldManager retrieveFrom(Map<String,Object> worldData) {
        return (BasicWorldManager) worldData.get(BASIC_WORLD_MANAGER_KEY);
    }

    /** Registers the BasicWorldManager with the GameManager */
    public void register() {
        gm.addGameActionListener(this);
        gm.addEntityActionsProcessor(this);
        gm.addTurnListener(this);
    }

    /** De-registers this BasicWorldManager from the GameManager. */
    public void deregister() {
        gm.removeGameActionListener(this);
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
        if (beforeAction)
            return false;  // we don't want to block the entity from handling the action itself

        if (action.equals(getDropAction())) {
            gm.moveEntity(e, e.getRoom());
            return true;
        } else if (action.equals(getTakeAction())) {
            if (gm.getPlayer().inventory.size() < maxInventoryItems)
                gm.takeEntity(e);
            else
                ui.appendTextLn(Meterman.getSystemBundle().getPassage("max-inventory-reached"));
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

    public void postAction(String action, Entity e, boolean actionHandled) {
        // empty
    }

    public void turn() {
        if (updateStatusBar)
            ui.setStatusLabel(UIConstants.RIGHT_LABEL, "Turns: " + (gm.getNumTurns() + 1));
    }
}
