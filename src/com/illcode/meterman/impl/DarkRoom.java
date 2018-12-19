package com.illcode.meterman.impl;

import com.illcode.meterman.Attributes;
import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;
import com.illcode.meterman.event.TurnListener;

import java.util.Collections;
import java.util.List;

import static com.illcode.meterman.Attributes.LIGHTSOURCE;
import static com.illcode.meterman.Attributes.DARK;

/**
 * A Room that handles {@link Attributes#DARK darkness} by returning a different name,
 * exit name, and description if the room is dark and there is no light source. It
 * will also conceal any entities in it if dark.
 */
public class DarkRoom extends BaseRoom implements TurnListener
{
    public String darkName, darkExitName, darkDescription;

    private boolean wasDark;  // used to detect changes in darkness at the end of each turn

    public DarkRoom() {
    }

    public void init() {
        super.init();
        darkName = "(dark name)";
        darkExitName = "(dark exit name)";
        darkDescription = "(dark description)";
    }

    public String getName() {
        if (isDark())
            return darkName;
        else
            return super.getName();
    }

    public String getExitName() {
        if (isDark())
            return darkExitName;
        else
            return super.getExitName();
    }

    public String getDescription() {
        if (isDark())
            return darkDescription;
        else
            return super.getDescription();
    }

    // You cannot see what's in the room if it's dark
    protected List<Entity> getRoomEntitiesImpl() {
        if (isDark())
            return Collections.emptyList();
        else
            return entities;
    }

    public boolean isDark() {
        // If we're not naturally dark, then it's definitely not dark
        if (!checkAttribute(DARK))
            return false;
        // Otherwise we'll see if something in the room or that the player is carrying is a light source
        for (Entity e : entities)
            if (e.checkAttribute(LIGHTSOURCE))
                return false;
        for (Entity e : Meterman.gm.getPlayer().inventory)
            if (e.checkAttribute(LIGHTSOURCE))
                return false;
        // DARKNESS! Charley Murphy!
        return true;
    }

    public void turn() {
        boolean nowDark = isDark();
        if (wasDark != nowDark) {
            wasDark = nowDark;
            Meterman.gm.roomChanged(this);
        }
    }

    public void entered(Room fromRoom) {
        Meterman.gm.addTurnListener(this);
        wasDark = isDark();
        super.entered(fromRoom);
    }

    public boolean exiting(Room toRoom) {
        boolean blocked = super.exiting(toRoom);
        if (!blocked)
            Meterman.gm.removeTurnListener(this);
        return blocked;
    }
}
