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

    private TextBundle b;
    private CloakState state;

    private BaseEntity cloak, hook, message;
    private BaseRoom cloakroom, foyer, patio;
    private DarkRoom bar;

    private List<String> actions;   // used in getActions()
    private List<Entity> darkBarEntities;  // entities found in the bar when it's dark
    private List<String> darkBarActions;  // actions available for those darkBarEntities

    void init(Map<String,BaseEntity> entityIdMap, Map<String,BaseRoom> roomIdMap, CloakState cloakState) {
        actions = new ArrayList<>(8);
        this.entityIdMap = entityIdMap;
        this.roomIdMap = roomIdMap;
        this.state = cloakState;
        b = Meterman.getSystemBundle();
        cloak = entityIdMap.get("cloak");
        hook = entityIdMap.get("hook");
        message = entityIdMap.get("scrawled-message");
        cloakroom = roomIdMap.get("cloakroom");
        bar = (DarkRoom) roomIdMap.get("bar");
        foyer = roomIdMap.get("foyer");
        patio = roomIdMap.get("patio");
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
                s += " " + b.getPassage("cloak-hung-description");
            return s;
        } else if (e == message) {
            if (state.numDarkBarActions < 3)
                return b.getPassage("winning-message");
            else
                return b.getPassage("losing-message");
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
                ui.appendTextLn(b.getPassage("unhang-cloak-message"));
            }
            return false;  // but let the regular machinery operate as usual
        } else if (action.equals(getHangOnHookAction()) || action.equals(getHangCloakAction())) {
            gm.moveEntity(cloak, cloakroom);  // drop it, if in inventory
            state.cloakHung = true;
            ui.appendTextLn(b.getPassage("hang-cloak-message"));
            return true;
        } else if (darkBarEntities.contains(e)) {
            ui.appendTextLn(b.getPassage("dark-bar-action-warning"));
            state.numDarkBarActions++;
            return true;
        } else {
            return false;
        }
    }

    public boolean suppressParserMessage(String action) {
        if (action.equals(getHangOnHookAction()) || action.equals(getHangCloakAction())) {
            ui.appendNewline();
            ui.appendTextLn(b.getPassage("hang-cloak-parser-message"));
            return true;
        } else {
            return false;
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
            ui.appendTextLn(b.getPassage("no-go-patio"));
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
