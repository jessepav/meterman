package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.TextBundle;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.BaseRoom;

import java.util.Map;

import static com.illcode.meterman.Meterman.ui;
import static com.illcode.meterman.Meterman.gm;

public class CloakManager implements PlayerMovementListener
{
    public static final String CLOAK_MANAGER_KEY = "CloakManager";

    private Map<String,BaseEntity> entityIdMap;
    private Map<String,BaseRoom> roomIdMap;

    private BaseRoom foyer, patio;

    private TextBundle b;

    public CloakManager() {
    }

    void init(Map<String,BaseEntity> entityIdMap, Map<String,BaseRoom> roomIdMap) {
        this.entityIdMap = entityIdMap;
        this.roomIdMap = roomIdMap;
        foyer = roomIdMap.get("foyer");
        patio = roomIdMap.get("patio");
        b = Meterman.getSystemBundle();  // includes our personal bundle
    }

    void register() {
        gm.addBeforePlayerMovementListener(this);
    }

    // Save this instance into the world data for later retrieval.
    public void saveTo(Map<String,Object> worldData) {
        worldData.put(CLOAK_MANAGER_KEY, this);
    }

    // Retrieve a previously saved instance from world data.
    public static CloakManager retrieveFrom(Map<String,Object> worldData) {
        return (CloakManager) worldData.get(CLOAK_MANAGER_KEY);
    }

    public boolean playerMove(Room from, Room to, boolean beforeMove) {
        if (beforeMove) {
            if (from == foyer && to == patio) {
                ui.appendTextLn(b.getPassage("no-go-patio"));
                return true;
            }
        }
        return false;
    }
}
