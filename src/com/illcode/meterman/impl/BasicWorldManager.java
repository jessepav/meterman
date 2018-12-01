package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Game;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.event.EntityActionProcessor;
import com.illcode.meterman.event.GameActionListener;

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
public class BasicWorldManager implements GameActionListener, EntityActionProcessor
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

        Meterman.gm.getWorldData().put(BASIC_WORLD_MANAGER_KEY, this);
    }

    /** Retrieve the BasicWorldManager instance (saved by {@link #install()}) from the
     *  WorldState's worldData */
    public static BasicWorldManager getBasicWorldManager() {
        return (BasicWorldManager) Meterman.gm.getWorldData().get(BASIC_WORLD_MANAGER_KEY);
    }

    public void processEntityActions(Entity e, List<String> actions) {

    }

    public boolean processAction(String action, Entity entity, boolean beforeAction) {
        return false;
    }
}
