package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CloakGame implements Game
{
    private static final String NAME = "Cloak of Darkness";

    static TextBundle bundle;

    public CloakGame() {
    }

    public String getGameName() {
        return NAME;
    }

    public void init() {
        Utils.setGameAssetsPath("cloakofdarkness");
        Utils.installActionNameTranslations(Utils.pathForGameAsset("cloak-action-translations.json"));
        bundle = TextBundle.loadBundle(Utils.pathForGameAsset("cloak-bundle.txt"));
        Meterman.setGameBundle(bundle);  // which also sets the bundle's parent to the system bundle
        Meterman.ui.loadImage("cloak", Utils.pathForGameAsset("cloak.png"));
        Meterman.ui.loadImage("phantom-frame-image", Utils.pathForGameAsset("phantom-frame-image.png"));
    }

    public void about() {
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
        Meterman.ui.setFrameImage("phantom-frame-image");
    }

    public void dispose() {

    }

    private void setupWorldState(WorldState worldState) {
        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        worldState.worldData.put("entityIdMap", wb.getEntityIdMap());
        worldState.worldData.put("roomIdMap", wb.getRoomIdMap());

        wb.loadRooms("cloak-rooms");
        wb.loadEntities("cloak-entities");
        wb.loadRoomConnections("room-connections");
        wb.loadEntityPlacements("entity-placements");

        BaseEntity cloak = wb.getEntity("cloak");
        cloak.setAttribute(Attributes.WEARABLE);
        cloak.setAttribute(Attributes.TAKEABLE);

        Player player = worldState.player;
        player.worn.add(cloak);
        player.currentRoom = wb.getRoom("foyer");
        // Note that the GameManager will fix up the consistency of inventory items when
        // a new game starts.

        BasicWorldManager basicWorldManager = new BasicWorldManager();
        basicWorldManager.saveTo(worldState.worldData);
        basicWorldManager.register();

        CloakState cloakState = new CloakState();
        cloakState.init();
        cloakState.saveTo(worldState.worldData);

        CloakDelegate cloakDelegate = new CloakDelegate();
        cloakDelegate.init(wb.getEntityIdMap(), wb.getRoomIdMap(), cloakState);
        // Delegate entities...
        for (String entityId : new String[] {"hook", "cloak", "scrawled-message"})
            wb.getEntity(entityId).setDelegate(cloakDelegate);
        // ...and rooms
        for (String roomId : new String[] {"foyer", "cloakroom", "bar"})
            wb.getRoom(roomId).setDelegate(cloakDelegate);
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
