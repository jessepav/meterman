package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.*;
import com.illcode.meterman.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.Meterman.ui;
import static com.illcode.meterman.games.cloakofdarkness.CloakActions.*;

public class CloakDelegate extends EntityDelegateAdapter implements RoomDelegate
{
    private Map<String,BaseEntity> entityIdMap;
    private Map<String,BaseRoom> roomIdMap;

    private CloakState state;
    private TextBundle bundle;

    private BaseEntity cloak, hook, message;
    private BaseRoom cloakroom, foyer, patio, purgatory;
    private DarkRoom bar;

    private List<String> actions;   // used in getActions()
    private List<Entity> darkBarEntities;  // entities found in the bar when it's dark
    private List<String> darkBarActions;  // actions available for those darkBarEntities

    void init(Map<String,BaseEntity> entityIdMap, Map<String,BaseRoom> roomIdMap, TextBundle bundle, CloakState cloakState) {
        actions = new ArrayList<>(8);
        this.entityIdMap = entityIdMap;
        this.roomIdMap = roomIdMap;
        this.state = cloakState;
        this.bundle = bundle;
        cloak = entityIdMap.get("cloak");
        hook = entityIdMap.get("hook");
        message = entityIdMap.get("scrawled-message");
        cloakroom = roomIdMap.get("cloakroom");
        bar = (DarkRoom) roomIdMap.get("bar");
        foyer = roomIdMap.get("foyer");
        patio = roomIdMap.get("patio");
        purgatory = roomIdMap.get("purgatory");
        darkBarEntities = new ArrayList<>(3);
        darkBarEntities.add(entityIdMap.get("dark-bar-junk1"));
        darkBarEntities.add(entityIdMap.get("dark-bar-junk2"));
        darkBarEntities.add(entityIdMap.get("dark-bar-junk3"));
        for (Entity e : darkBarEntities)
            ((BaseEntity) e).setDelegate(this);
        darkBarActions = new ArrayList<>(3);
        darkBarActions.add(getProdAction());
        darkBarActions.add(getPokeAction());
        darkBarActions.add(getKickAction());
    }

    //region -- EntityDelegate --

    public String getDescription(BaseEntity e) {
        if (e == cloak) {
            String s = e.description;
            if (state.cloakHung)
                s += " " + bundle.getPassage("cloak-hung-description");
            return s;
        } else {
            return e.description;
        }
    }

    public List<String> getActions(BaseEntity e) {
        actions.clear();
        if (gm.getCurrentRoom() == cloakroom) {
            if (e == cloak) {
                if (!state.cloakHung)
                    actions.add(getHangOnHookAction());
            } else if (e == hook) {
                if (!state.cloakHung)
                    actions.add(getHangCloakAction());
            }
        } else if (darkBarEntities.contains(e)) {
            return darkBarActions;
        }
        return actions;
    }

    public boolean processAction(BaseEntity e, String action) {
        if (e == cloak && action.equals(BasicActions.getTakeAction())) {
            if (state.cloakHung) {
                state.cloakHung = false;  // can't be hung if we took it!
                ui.appendTextLn(bundle.getPassage("unhang-cloak-message"));
            }
            return false;  // but let the regular machinery operate as usual
        } else if (action.equals(getHangOnHookAction()) || action.equals(getHangCloakAction())) {
            gm.moveEntity(cloak, cloakroom);  // drop it, if in inventory
            state.cloakHung = true;
            ui.appendTextLn(bundle.getPassage("hang-cloak-message"));
            return true;
        } else if (darkBarEntities.contains(e)) {
            ui.appendTextLn(bundle.getPassage("dark-bar-action-warning"));
            state.numDarkBarActions++;
            return true;
        } else {
            return false;
        }
    }

    public String replaceParserMessage(BaseEntity e, String action) {
        if (action.equals(getHangOnHookAction()) || action.equals(getHangCloakAction())) {
            return bundle.getPassage("hang-cloak-parser-message");
        } else {
            return null;
        }
    }

    public boolean selected(BaseEntity e) {
        if (e == message) {
            gm.undoCheckpoint();
            // I use putSubstitution() and getPassageSplit() here just to show their operation.
            bundle.putSubstitution("wonlost",
                bundle.getPassageSplit("win-lose", '|')[state.numDarkBarActions < 3 ? 0 : 1]);
            String s = bundle.getPassage("endgame-message");
            bundle.clearSubstitutions();
            ui.showTextDialog("Message", s, "Close");
            ui.clearText();
            bundle.putPassage("wait-message", bundle.getPassage("purgatory-wait-message"));
            gm.movePlayer(purgatory);
            return true;
        } else {
            return super.selected(e);
        }
    }

    //endregion

    //region -- RoomDelegate --

    public String getDescription(BaseRoom r) {
        return r.description;
    }

    public void entered(BaseRoom r, Room fromRoom) {
    }

    public boolean exiting(BaseRoom r, Room toRoom) {
        if (r == cloakroom) {
            if (!gm.isInInventory(cloak) && cloak.getRoom() == cloakroom)
                bar.clearAttribute(Attributes.DARK);
            else
                bar.setAttribute(Attributes.DARK);
        } else if (r == foyer && toRoom == patio) {
            ui.appendTextLn(bundle.getPassage("no-go-patio"));
            return true;
        }
        return false;
    }

    public List<Entity> getRoomEntities(BaseRoom r) {
        if (r == bar && bar.isDark())
            return darkBarEntities;
        else
            return r.entities;
    }

    //endregion
}
