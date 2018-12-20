package com.illcode.riverboat;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.BasicActions;
import com.illcode.meterman.impl.WorldBuilder;

import java.util.Map;

class RiverboatManagerPart1 implements GameActionListener
{
    private static final String RIVERBOAT_MANAGER_PART1_KEY = "RiverboatManagerPart1";

    private WorldBuilder worldBuilder;
    private Map<String,Object> worldData;

    private BaseEntity leaf, chest;

    RiverboatManagerPart1() {
    }

    void init(WorldBuilder wb, Map<String,Object> worldData) {
        this.worldBuilder = wb;
        this.worldData = worldData;

        leaf = wb.getEntity("leaf");
        chest = wb.getEntity("chest");
    }

    void saveTo(Map<String,Object> worldData) {
        worldData.put(RIVERBOAT_MANAGER_PART1_KEY, this);
    }

    static RiverboatManagerPart1 retrieveFrom(Map<String,Object> worldData) {
        return (RiverboatManagerPart1) worldData.get(RIVERBOAT_MANAGER_PART1_KEY);
    }

    void register() {
        Meterman.gm.addGameActionListener(this);
    }

    void deregister() {
        Meterman.gm.removeGameActionListener(this);
    }

    public boolean processAction(String action, Entity e, boolean beforeAction) {
        return false;
    }

    public void postAction(String action, Entity e, boolean actionHandled) {
        if (e == chest) {
            if (action.equals(BasicActions.getUnlockAction())) {
                leaf.listName = "Funky Leaf";
                Meterman.gm.entityChanged(leaf);
            } else if (action.equals(BasicActions.getLockAction())) {
                leaf.listName = "Normal Leaf";
                Meterman.gm.entityChanged(leaf);
            }
        }
    }
}
