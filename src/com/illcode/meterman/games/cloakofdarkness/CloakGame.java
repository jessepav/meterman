package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.BaseRoom;
import com.illcode.meterman.impl.WorldBuilder;

import java.util.HashMap;
import java.util.Map;

public class CloakGame implements Game
{
    private static final String NAME = "Cloak of Darkness";

    private TextBundle bundle;

    public CloakGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public void init() {
        Utils.installActionNameTranslations(Utils.pathForAsset("cloakofdarkness/cloak-action-translations.json"));
        bundle = TextBundle.loadBundle(Utils.pathForAsset("cloakofdarkness/cloak-bundle.txt"));
        Meterman.setGameBundle(bundle);  // which also sets the bundle's parent to the system bundle
    }

    public void about() {
        Meterman.ui.appendNewline();
        Meterman.ui.appendTextLn(bundle.getPassage("about-text"));
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.gameName = NAME;
        worldState.player = new Player();
        worldState.player.init();
        worldState.worldData = new HashMap<>();
        worldState.numTurns = 0;

        setupWorldState(worldState);
        return worldState;
    }

    public void start(boolean newGame) {
    }

    public void dispose() {

    }

    private void setupWorldState(WorldState worldState) {
        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        worldState.worldData.put("entityIdMap", wb.getEntityIdMap());
        worldState.worldData.put("roomIdMap", wb.getRoomIdMap());
    }

    @SuppressWarnings("unchecked")
    public static Map<String,BaseEntity> retrieveEntityIdMap(Map<String,Object> worldData) {
        return (Map<String,BaseEntity>) worldData.get("entityIdMap");
    }

    @SuppressWarnings("unchecked")
    public static Map<String,BaseRoom> retrieveRoomIdMap(Map<String,Object> worldData) {
        return (Map<String,BaseRoom>) worldData.get("roomIdMap");
    }
}
