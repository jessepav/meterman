package com.illcode.meterman.impl;

import com.illcode.meterman.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.Meterman.ui;
import static com.illcode.meterman.Utils.fmt;
import static com.illcode.meterman.impl.BasicActions.*;

/**
 * This class represents anything that can contain, support, or in a similar way hold a set of entities as
 * contents. It supports being locked and opened with a key.
 * <p/>
 * Thanks to special support in {@link DarkRoom}, unlocked containers in the room (as opposed to in the player's
 * inventory) that contain light sources will propagate light, as it were, into the room. This only works
 * one level deep--a light source in a container inside another container will not propagate light all the way
 * into the room.
 */
public class Container extends BaseEntity
{
    /** The preposition that will be used when putting something "in/on/etc." this container.
     *  Should generally be in lowercase. */
    protected String inPrep;

    /** The preposition that will be used when taking something "out/off/etc." of this container.
     *  Should generally be in lowercase. */
    protected String outPrep;

    protected List<Entity> contents; // The items we contain.
    protected boolean locked;
    protected Entity key;
    protected List<String> actions;
    protected LinkedList<ContainerListener> containerListeners;

    private TextBundle bundle;


    public Container() {
    }

    public void init() {
        super.init();
        inPrep = "(in)";
        outPrep = "(out)";
        contents = new ArrayList<>(8);
        actions = new ArrayList<>(6);
        containerListeners = new LinkedList<>();
        bundle = Meterman.getSystemBundle();
    }

    public String getDescription() {
        if (!locked)
            return description;
        else
            return description + " " + bundle.getPassage("container-locked-message");
    }

    public void setRoom(Room room) {
        super.setRoom(room);
        for (Entity e : contents)
            e.setRoom(room);
    }

    public Entity getKey() {
        return key;
    }

    public void setKey(Entity key) {
        this.key = key;
        if (key == null)
            locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<Entity> getContents() {
        return contents;
    }

    public List<String> getActions() {
        actions.clear();
        if (locked) {
            actions.add(getUnlockAction());
        } else {
            actions.add(getContainerExamineAction());
            actions.add(getContainerPutAction(inPrep));
            actions.add(getContainerTakeAction(outPrep));
            if (key != null)
                actions.add(getLockAction());
        }
        return actions;
    }

    public boolean processAction(String action) {
        try {
            bundle.putSubstitution("defName", GameUtils.defName(this));
            bundle.putSubstitution("inPrep", inPrep);
            if (action.equals(getLockAction()) || action.equals(getUnlockAction())) {  // LOCK/UNLOCK
                // note that in these cases we already know that key != null
                if (!Meterman.gm.isInInventory(key)) {
                    ui.appendTextLn(bundle.getPassage("container-no-key-message"));
                } else {
                    locked = !locked;
                    gm.entityChanged(this);
                }
                return true;
            } else if (action.equals(getContainerExamineAction())) {  // EXAMINE ITEMS
                if (contents.isEmpty()) {
                    ui.appendTextLn(fmt("\n> %s %s %s", getContainerExamineAction(), inPrep, getName()).toUpperCase());
                    ui.appendTextLn(bundle.getPassage("container-no-contents-examine-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), bundle.getPassage("container-examine-message"), contents, true);
                    if (item != null) {
                        ui.appendTextLn(fmt("\n> %s %s", getExamineAction(), item.getName()).toUpperCase());
                        ui.appendTextLn(item.getDescription());
                    }
                }
                return true;
            } else if (action.equals(getContainerPutAction(inPrep))) {  // PUT IN
                List<Entity> takeables = new ArrayList<>();
                GameUtils.getCurrentTakeableEntities(takeables);
                if (checkAttribute(Attributes.TAKEABLE))
                    takeables.remove(this);
                if (takeables.isEmpty()) {
                    ui.appendTextLn(fmt("\n> %s %s", getContainerPutAction(inPrep), getName()).toUpperCase());
                    ui.appendTextLn(bundle.getPassage("container-no-contents-put-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), bundle.getPassage("container-put-message"), takeables, true);
                    if (item != null) {
                        if (!fireContentsChange(item, true, true)) {  // if we're not blocked
                            ui.appendTextLn(fmt("\n> %s %s %s %s",
                                getPutAction(), item.getName(), inPrep, getName()).toUpperCase());
                            Room currentRoom = gm.getCurrentRoom();
                            gm.moveEntity(item, currentRoom);  // pull it out of inventory, if it's there
                            currentRoom.getRoomEntities().remove(item);  // whisk it out of of the room
                            gm.roomChanged(currentRoom);
                            contents.add(item);  // and now it's in here!
                            fireContentsChange(item, true, false);
                        }
                    }
                }
                return true;
            } else if (action.equals(getContainerTakeAction(outPrep))) {  // TAKE FROM
                List<Entity> takeables = new ArrayList<>();
                GameUtils.filterByAttribute(contents, Attributes.TAKEABLE, true, takeables);
                if (takeables.isEmpty()) {
                    ui.appendTextLn(fmt("\n> %s %s", getContainerTakeAction(outPrep), getName()).toUpperCase());
                    ui.appendTextLn(bundle.getPassage("container-no-contents-take-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), bundle.getPassage("container-take-message"), takeables, true);
                    if (item != null) {
                        if (!fireContentsChange(item, false, true)) {  // if we're not blocked
                            ui.appendTextLn(fmt("\n> %s %s %s %s",
                                getTakeAction(), item.getName(), outPrep, getName()).toUpperCase());
                            contents.remove(item);
                            gm.takeEntity(item);
                            fireContentsChange(item, false, false);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } finally {
            bundle.removeSubstitution("defName");
            bundle.removeSubstitution("inPrep");
        }
    }

    public String replaceParserMessage(String action) {
        if (action.equals(getContainerExamineAction()) ||
            action.equals(getContainerPutAction(inPrep)) ||
            action.equals(getContainerTakeAction(outPrep)))
            return "";  // we'll print our own parser message
        else
            return super.replaceParserMessage(action);
    }

    //region -- Listener list methods --
    /**
     * Add a ContainerListener to be notified when something is added to or removed from this container.
     * @param l listener
     */
    public void addContainerListener(ContainerListener l) {
        if (!containerListeners.contains(l))
            containerListeners.addFirst(l);
    }

    /** Remove a ContainerListener. */
    public void removeContainerListener(ContainerListener l) {
        containerListeners.remove(l);
    }

    private boolean fireContentsChange(Entity e, boolean isAdded, boolean beforeEntityMove) {
        for (ContainerListener l : containerListeners)
            if (l.contentsChange(this, e, isAdded, beforeEntityMove))
                return true;
        return false;
    }
    //endregion
}
