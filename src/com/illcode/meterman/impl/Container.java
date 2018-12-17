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
 * This class represents anything that can contain, support, or in any
 * similar way hold a set of entities as contents.
 */
public class Container extends BaseEntity
{
    /** The preposition that will be used when putting something "in/on/etc." this container. */
    protected String inPrep;

    /** The preposition that will be used when taking something "out/off/etc." of this container. */
    protected String outPrep;

    protected List<Entity> contents; // The items we contain.
    protected boolean locked;
    protected Entity key;
    protected List<String> actions;
    protected LinkedList<ContainerListener> containerListeners;

    private TextBundle sysBundle;


    public Container() {
    }

    public void init() {
        inPrep = "(in)";
        outPrep = "(out)";
        // We use an ArrayList because these items will most often be shown in a ListDialog,
        // and the showListDialog method uses random access.
        contents = new ArrayList<>(8);
        actions = new ArrayList<>(6);
        containerListeners = new LinkedList<>();
        sysBundle = Meterman.getSystemBundle();
    }

    public String getDescription() {
        if (!locked)
            return description;
        else
            return description + " " + sysBundle.getPassage("container-locked-message");
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
            sysBundle.putSubstitution("defName", GameUtils.defName(this));
            sysBundle.putSubstitution("inPrep", inPrep);
            if (action.equals(getLockAction()) || action.equals(getUnlockAction())) {
                // note that in these cases we already know that key != null
                if (!Meterman.gm.isInInventory(key)) {
                    ui.appendTextLn(sysBundle.getPassage("container-no-key-message"));
                } else {
                    locked = !locked;
                    gm.entityChanged(this);
                }
                return true;
            } else if (action.equals(getContainerExamineAction())) {
                if (contents.isEmpty()) {
                    ui.appendTextLn(sysBundle.getPassage("container-no-contents-examine-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), sysBundle.getPassage("container-examine-message"), contents, true);
                    if (item != null) {
                        ui.appendTextLn(fmt("\n> %s %s", getExamineAction(), item.getName()).toUpperCase());
                        ui.appendTextLn(item.getDescription());
                    }
                }
                return true;
            } else if (action.equals(getContainerPutAction(inPrep))) {
                List<Entity> takeables = new ArrayList<>();
                GameUtils.getCurrentTakeableEntities(takeables);
                if (checkAttribute(Attributes.TAKEABLE))
                    takeables.remove(this);
                if (takeables.isEmpty()) {
                    ui.appendTextLn(sysBundle.getPassage("container-no-contents-put-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), sysBundle.getPassage("container-put-message"), takeables, true);
                    if (item != null) {
                        if (!fireContentsChanging(item, true)) {  // if we're not blocked
                            ui.appendTextLn(fmt("\n> %s %s %s %s",
                                getPutAction(), item.getName(), inPrep, getName()).toUpperCase());
                            Room currentRoom = gm.getCurrentRoom();
                            gm.moveEntity(item, currentRoom);  // pull it out of inventory, if it's there
                            currentRoom.getRoomEntities().remove(item);  // whisk it out of of the room
                            gm.roomChanged(currentRoom);
                            contents.add(item);  // and now it's in here!
                        }
                    }
                }
                return true;
            } else if (action.equals(getContainerTakeAction(outPrep))) {
                List<Entity> takeables = new ArrayList<>();
                GameUtils.filterByAttribute(contents, Attributes.TAKEABLE, true, takeables);
                if (takeables.isEmpty()) {
                    ui.appendTextLn(sysBundle.getPassage("container-no-contents-take-message"));
                } else {
                    Entity item = ui.showListDialog(getName(), sysBundle.getPassage("container-take-message"), takeables, true);
                    if (item != null) {
                        if (!fireContentsChanging(item, false)) {  // if we're not blocked
                            ui.appendTextLn(fmt("\n> %s %s %s %s",
                                getTakeAction(), item.getName(), outPrep, getName()).toUpperCase());
                            contents.remove(item);
                            gm.takeEntity(item);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } finally {
            sysBundle.removeSubstitution("defName");
            sysBundle.removeSubstitution("inPrep");
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

    private boolean fireContentsChanging(Entity e, boolean isAdded) {
        for (ContainerListener l : containerListeners)
            if (l.contentsChanging(this, e, isAdded))
                return true;
        return false;
    }
    //endregion
}
