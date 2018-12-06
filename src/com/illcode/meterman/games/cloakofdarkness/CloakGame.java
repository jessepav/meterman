package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.*;

import java.util.HashMap;
import java.util.Map;

import static com.illcode.meterman.ui.UIConstants.*;

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
        Meterman.ui.loadImage("cloak", Utils.pathForAsset("cloakofdarkness/cloak.png"));
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
    }

    public void dispose() {

    }

    private void setupWorldState(WorldState worldState) {
        WorldBuilder wb = new WorldBuilder(worldState, bundle);
        worldState.worldData.put("entityIdMap", wb.getEntityIdMap());
        worldState.worldData.put("roomIdMap", wb.getRoomIdMap());

        BaseRoom foyer = wb.loadRoom("foyer");
        DarkRoom bar = wb.loadDarkRoom("bar");
        BaseRoom cloakroom = wb.loadRoom("cloakroom");
        BaseEntity cloak = wb.loadEntity("cloak");
        BaseEntity hook = wb.loadEntity("hook");
        BaseEntity message = wb.loadEntity("scrawled-message");
        wb.loadEntity("dark-bar-junk1");
        wb.loadEntity("dark-bar-junk2");
        wb.loadEntity("dark-bar-junk3");

        cloak.setAttribute(Attributes.WEARABLE);
        cloak.setAttribute(Attributes.TAKEABLE);

        // The patio is a "fake" room that the player will not be allowed to move to
        BaseRoom patio = new BaseRoom();
        patio.init();
        patio.id = "patio";
        patio.name = patio.exitName = "Patio";
        wb.putRoom(patio);

        wb.connectRooms("foyer", S_BUTTON, "bar", N_BUTTON);
        wb.connectRooms("foyer", W_BUTTON, "cloakroom", E_BUTTON);
        wb.connectRoomOneWay("foyer", N_BUTTON, "patio");

        wb.putEntitiesInRoom("cloakroom", "hook");
        wb.putEntitiesInRoom("bar", "scrawled-message");

        Player player = worldState.player;
        player.inventory.add(cloak);
        player.worn.add(cloak);
        player.currentRoom = foyer;
        for (Entity e : player.inventory)
            e.setRoom(player.currentRoom);

        BasicWorldManager basicWorldManager = new BasicWorldManager();
        basicWorldManager.saveTo(worldState.worldData);
        basicWorldManager.register();

        CloakState cloakState = new CloakState();
        cloakState.init();
        cloakState.saveTo(worldState.worldData);

        CloakDelegate cloakDelegate = new CloakDelegate();
        cloakDelegate.init(wb.getEntityIdMap(), wb.getRoomIdMap(), cloakState);
        // Delegate entities
        hook.setDelegate(cloakDelegate);
        cloak.setDelegate(cloakDelegate);
        message.setDelegate(cloakDelegate);
        // and rooms
        foyer.setDelegate(cloakDelegate);
        cloakroom.setDelegate(cloakDelegate);
        bar.setDelegate(cloakDelegate);
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
