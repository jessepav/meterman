package com.illcode.cloak;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.BasicWorldManager;
import com.illcode.meterman.impl.WorldBuilder;

public class CloakGame implements Game
{
    private static final String NAME = "Cloak of Darkness";

    TextBundle bundle;

    public CloakGame() {
    }

    public String getName() {
        return NAME;
    }

    public void init() {
        Utils.installActionNameTranslations(Utils.pathForGameAsset("cloak-action-translations.json"));
        bundle = TextBundle.loadBundle(Utils.pathForGameAsset("cloak-bundle.txt"));
        Meterman.setGameBundle(bundle);  // which also sets the bundle's parent to the system bundle
        Meterman.ui.loadImage("cloak", Utils.pathForGameAsset("cloak.png"));
        Meterman.ui.loadImage("phantom-frame-image", Utils.pathForGameAsset("phantom-frame-image.png"));
    }

    public void about() {
        Meterman.ui.showTextDialog("Cloak of Darkness", bundle.getPassage("about-text"), "OK");
    }

    public WorldState getInitialWorldState() {
        WorldState worldState = new WorldState();
        worldState.init(NAME);

        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        wb.saveTo(worldState.worldData);

        wb.loadRooms("cloak-rooms");
        wb.loadEntities("cloak-entities");
        wb.loadRoomConnections("room-connections");
        wb.loadEntityPlacements("entity-placements");
        wb.loadPlayerState("player-state");

        BasicWorldManager basicWorldManager = new BasicWorldManager();
        basicWorldManager.saveTo(worldState.worldData);
        basicWorldManager.register();

        CloakState cloakState = new CloakState();
        cloakState.init();
        cloakState.saveTo(worldState.worldData);

        CloakDelegate cloakDelegate = new CloakDelegate();
        cloakDelegate.init(wb.getEntityIdMap(), wb.getRoomIdMap(), bundle, cloakState);
        // Delegate entities...
        for (String entityId : new String[] {"hook", "cloak", "scrawled-message"})
            wb.getEntity(entityId).setDelegate(cloakDelegate);
        // ...and rooms
        for (String roomId : new String[] {"foyer", "cloakroom", "bar"})
            wb.getRoom(roomId).setDelegate(cloakDelegate);

        return worldState;
    }

    public void start(boolean newGame) {
        Meterman.ui.setFrameImage("phantom-frame-image");
        if (newGame) {
            Meterman.sound.loadMusic("intro-music", Utils.pathForGameAsset("intro-music.ogg"));
            Meterman.sound.playMusic("intro-music", false, 1.0);
        }
    }

    public void dispose() {

    }

    public void debugCommand(String command) {
        Utils.logger.fine("Debug Command: " + command);
    }
}
