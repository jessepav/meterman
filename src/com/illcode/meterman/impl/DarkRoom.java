package com.illcode.meterman.impl;

import com.illcode.meterman.Attributes;
import com.illcode.meterman.Entity;
import com.illcode.meterman.Meterman;

import java.util.Collections;
import java.util.List;

import static com.illcode.meterman.Attributes.LIGHTSOURCE;
import static com.illcode.meterman.Attributes.DARK;

/**
 * A Room that handles {@link Attributes#DARK darkness} by returning a different name,
 * exit name, and description if the room is dark and there is no light source. It
 * will also conceal any entities in it if dark.
 */
public class DarkRoom extends BaseRoom
{
    public String darkName, darkExitName, darkDescription;

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
    public List<Entity> getRoomEntities() {
        if (delegate != null)
            return delegate.getRoomEntities(this);

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
}
