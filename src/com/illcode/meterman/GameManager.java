package com.illcode.meterman;

import com.illcode.meterman.event.EntitySelectionListener;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.games.GamesList;
import com.illcode.meterman.ui.UIConstants;

import java.util.*;

import static com.illcode.meterman.Meterman.ui;

public final class GameManager
{
    /** The game we're currently playing */
    private Game game;

    /** The current state of the world */
    private WorldState worldState;

    // Elements of worldState, here for easy access
    private Player player;
    private List<Room> rooms;
    private Map<String,Object> worldData;

    // Our listener lists
    private LinkedList<GameActionListener> beforeGameActionListeners;
    private LinkedList<GameActionListener> defaultGameActionListeners;
    private LinkedList<PlayerMovementListener> beforePlayerMovementListeners;
    private LinkedList<PlayerMovementListener> afterPlayerMovementListeners;
    private LinkedList<TurnListener> beforeTurnListeners;
    private LinkedList<TurnListener> afterTurnListeners;
    private LinkedList<EntitySelectionListener> entitySelectionListeners;

    // To be used in composing text before sending it off to the UI
    private StringBuilder sb;

    // Used for composing UI actions - reuse same list to avoid allocation
    private List<String> actions;

    private Entity selectedEntity;  // currently selected entity, or null if none

    public GameManager() {
    }

    public void init() {
        beforeGameActionListeners = new LinkedList<>();
        defaultGameActionListeners = new LinkedList<>();
        beforePlayerMovementListeners = new LinkedList<>();
        afterPlayerMovementListeners = new LinkedList<>();
        beforeTurnListeners = new LinkedList<>();
        afterTurnListeners = new LinkedList<>();
        entitySelectionListeners = new LinkedList<>();
        sb = new StringBuilder(2048);
        actions = new ArrayList<>(16);
    }

    public void dispose() {
        game = null;
        worldState = null;
        player = null;
        rooms = null;
        worldData = null;
        beforeGameActionListeners = null;
        defaultGameActionListeners = null;
        beforePlayerMovementListeners = null;
        afterPlayerMovementListeners = null;
        beforeTurnListeners = null;
        afterTurnListeners = null;
        entitySelectionListeners = null;
        sb = null;
        actions = null;
    }

    /**
     * Start a new game.
     * Note that no listeners will be notified nor callbacks invoked at the start
     * of the first turn when the game begins.
     * @param game game to start
     */
    public void newGame(Game game) {
        this.game = game;
        worldState = game.getInitialWorldState();
        player = worldState.player;
        rooms = worldState.rooms;
        worldData = worldState.worldData;
        game.start(true);
    }

    /**
     * Resume a game given a restored WorldState. While the world model graph will
     * be as it was when the game was saved, the UI may be slightly different (no images,
     * and definitely no scrollback in the main text area).
     * @param worldState WorldState to restore
     */
    public void loadGame(WorldState worldState) {
        this.worldState = worldState;
        game = GamesList.getGame(worldState.gameName);
        player = worldState.player;
        rooms = worldState.rooms;
        worldData = worldState.worldData;
        refreshRoomUI(getCurrentRoom());
        refreshInventoryUI();
        entitySelected(null);
        game.start(false);
    }

    public Player getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return player.currentRoom;
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    /**
     * Moves the player to a destination room. All appropriate listeners will be notified, and
     * one of them may cancel this move.
     * @param toRoom the room to which the player should move.
     */
    // Note that before even arriving here, the current room has a chance to block
    // UI-initiated movement in exitSelected() by returning false from attemptExit()
    public void movePlayer(Room toRoom) {
        if (toRoom == null || toRoom == player.currentRoom)
            return;
        Room fromRoom = player.currentRoom;
        // Here we go...
        if (fireBeforePlayerMovementEvent(fromRoom, toRoom))
            return;  // we were blocked by a listener
        for (Entity e : fromRoom.getRoomEntities())
            e.exitingScope();
        fromRoom.exiting();
        player.currentRoom = toRoom;
        toRoom.entered();
        for (Entity e : player.inventory)
            e.setRoom(toRoom);
        for (Entity e : toRoom.getRoomEntities())
            e.enterScope();
        fireAfterPlayerMovementEvent(fromRoom, toRoom);
        entitySelected(null);
        lookAction();
        refreshRoomUI(toRoom);
    }

    /**
     * Moves an entity to a room. The entity can currently reside in a room, in player inventory,
     * or nowhere.
     * @param e entity to move
     * @param r destination room, or null if the entity should be removed from the game world
     */
    public void moveEntity(Entity e, Room r) {
        Room previousRoom = e.getRoom();
        Room playerRoom = player.currentRoom;
        if (isEntityInInventory(e)) {
            player.worn.remove(e);
            player.equipped.remove(e);
            player.inventory.remove(e);
            e.dropped();
            refreshInventoryUI();
        } else {
            if (previousRoom != null)
                previousRoom.getRoomEntities().remove(e);
        }
        if (previousRoom != r) {
            if (previousRoom == playerRoom)
                e.exitingScope();
            else if (r == playerRoom)
                e.enterScope();
            e.setRoom(r);
        }
        if (r != null)
            r.getRoomEntities().add(e);
        if (previousRoom == playerRoom || r == playerRoom)
            refreshRoomUI(playerRoom);
    }

    /**
     * Takes an entity (i.e. moves an entity to the player inventory).
     * @param e entity to take
     */
    public void takeEntity(Entity e) {
        if (!isEntityInInventory(e)) {
            Room previousRoom = e.getRoom();
            Room playerRoom = player.currentRoom;
            if (previousRoom != null)
                previousRoom.getRoomEntities().remove(e);
            player.inventory.add(e);
            e.taken();
            if (previousRoom != playerRoom) {
                e.setRoom(playerRoom);
                e.enterScope();
            }
            refreshInventoryUI();
        }
    }

    /** Returns true if the given entity is in the player inventory. */
    public boolean isEntityInInventory(Entity e) {
        return player.inventory.contains(e);
    }

    /** Returns true if the entity is being worn by the player */
    public boolean isEntityWorn(Entity e) {
        return player.worn.contains(e);
    }

    /**
     * Sets whether an entity is worn by the player.
     * @param e entity to wear
     * @param wear whether the player should wear the entity. If true, and the given entity is in
     *      the player inventory and is {@link Attributes#WEARABLE wearable}, the player will wear it.
     * @return true if the operation succeeded
     */
    public boolean setEntityWorn(Entity e, boolean wear) {
        if (wear) {
            if (e.checkAttribute(Attributes.WEARABLE) && isEntityInInventory(e) && !isEntityWorn(e)) {
                player.worn.add(e);
                return true;
            }
        } else {
            if (isEntityWorn(e)) {
                player.worn.remove(e);
                return true;
            }
        }
        return false;
    }

    /** Returns true if the entity is equipped by the player */
    public boolean isEntityEquipped(Entity e) {
        return player.equipped.contains(e);
    }

    /**
     * Sets whether an entity is equipped by the player.
     * @param e entity to equip
     * @param equip whether the player should equip the entity. If true, and the given entity is in
     *      the player inventory and is {@link Attributes#EQUIPPABLE equippable}, the player will equip it.
     * @return true if the operation succeeded
     */
    public boolean setEntityEquipped(Entity e, boolean equip) {
        if (equip) {
            if (e.checkAttribute(Attributes.EQUIPPABLE) && isEntityInInventory(e) && !isEntityEquipped(e)) {
                player.equipped.add(e);
                return true;
            }
        } else {
            if (isEntityEquipped(e)) {
                player.equipped.remove(e);
                return true;
            }
        }
        return false;
    }

    /** Called by the UI when the user selects the "About..." menu item.*/
    public void aboutMenuClicked() {
        game.about();
    }

    /** Called by the UI when the user clicks "Look" */
    public void lookAction() {
        sb.setLength(0);
        sb.append("\n");
        sb.append(getCurrentRoom().getDescription());
        sb.append("\n");
        ui.appendText(sb.toString());
        sb.setLength(0);
        nextTurn();
    }

    /** Called by the UI when the user clicks "Wait" */
    public void waitAction() {
        nextTurn();
    }

    /** Moves from one turn to the next */
    private void nextTurn() {
        fireAfterTurnEvent();
        fireBeforeTurnEvent();
    }
    
    /** Called by the UI when the user clicks an exit button */
    public void exitSelected(int direction) {
        movePlayer(getCurrentRoom().getExit(direction));
        nextTurn();
    }

    /** Called by the UI when the user clicks an action button (or selects an action
     *  from the combo box when there are many actions) */
    public void entityActionSelected(String action) {
        if (!fireBeforeActionEvent(action, selectedEntity)) {
            if (!selectedEntity.processAction(action))
                fireDefaultActionEvent(action, selectedEntity);
        }
        nextTurn();
    }

    /**
     * Called by the UI to indicate that the user selected an entity in the lists.
     * @param e entity selected, or null if no entity selected
     */
    public void entitySelected(Entity e) {
        selectedEntity = e;
        actions.clear();
        if (e == null) {
            ui.clearActions();
            ui.setObjectName("(nothing selected)");
            ui.setObjectText("");
            ui.setEntityImage(null);
        } else {
            e.selected();
            actions.addAll(e.getActions());
            fireEntitySelectedEvent(e, actions);
            refreshEntityUI(e);
        }
    }

    /**
     * Called by an entity when its internal state changes in such a way that the UI
     * may have to be updated to reflect the change.
     * @param e entity that has changed
     */
    public void refreshEntityUI(Entity e) {
        if (e == selectedEntity) {
            ui.setObjectName(e.getName());
            ui.setObjectText(e.getDescription());
            ui.clearActions();
            for (String s : actions)
                ui.addAction(s);
        }
    }

    /**
     * Called by a room when its internal state changes in such a way that the UI
     * may have to be updated to reflect the change.
     * @param r room that has changed
     */
    public void refreshRoomUI(Room r) {
        if (r == getCurrentRoom()) {
            for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
                ui.setExitLabel(pos, r.getExitLabel(pos));
            ui.clearRoomEntities();
            for (Entity e : r.getRoomEntities())
                if (!e.checkAttribute(Attributes.CONCEALED))
                    ui.addRoomEntity(e);
        }
    }

    /**
     * Called when the player inventory changes in such a way that the UI needs to be refreshed.
     */
    public void refreshInventoryUI() {
        HashSet<Entity> remainingItems = new HashSet<>(player.inventory);
        ui.clearInventoryEntities();
        for (Entity item : player.equipped) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item);
        }
        for (Entity item : player.worn) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item);
        }
        for (Entity item : player.inventory) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item);
        }
    }

    //region Event Listener methods
    /**
     * Adds a GameActionListener to be called before a game action is processed. If the listener's
     * {@link GameActionListener#processAction(String, Entity)} method returns true, further action
     * processing will be bypassed.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addBeforeGameActionListener(GameActionListener l) {
        if (!beforeGameActionListeners.contains(l))
            beforeGameActionListeners.addFirst(l);
    }


    /**
     * Removes a GameActionListener from the before-action notification list.
     * @param l listener to remove
     */
    public void removeBeforeGameActionListener(GameActionListener l) {
        beforeGameActionListeners.remove(l);
    }

    /**
     * Adds a GameActionListener to be called if an action was not handled by any
     * {@link #addBeforeGameActionListener before-action listeners} or by the
     * {@link Entity#processAction(String) selected entity}.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addDefaultGameActionListener(GameActionListener l) {
        if (!defaultGameActionListeners.contains(l))
            defaultGameActionListeners.addFirst(l);
    }

    /**
     * Removes a GameActionListener from the default-action notification list.
     * @param l listener to remove
     */
    public void removeDefaultGameActionListener(GameActionListener l) {
        defaultGameActionListeners.remove(l);
    }

    /**
     * Notifies all registered {@code GameActionListener}S that an action is about
     * to be processed.
     * @param action action name
     * @param e selected entity
     * @return true if any GameActionListener interrupted the chain by returning true,
     *         and thus normal action processing should be skipped.
     */
    private boolean fireBeforeActionEvent(String action, Entity e) {
        for (GameActionListener l : beforeGameActionListeners) {
            if (l.processAction(action, e))
                return true;
        }
        return false;
    }

    /**
     * Notifies registered default-action {@code GameActionListener}S that an action is
     * being processed that was not handled by before-action listeners or the entity itself.
     * @param action action name
     * @param e selected entity
     * @return true if any GameActionListener interrupted the chain by returning true,
     */
    private boolean fireDefaultActionEvent(String action, Entity e) {
        for (GameActionListener l : defaultGameActionListeners) {
            if (l.processAction(action, e))
                return true;
        }
        return false;
    }

    /**
     * Adds a PlayerMovementListener to be called before player movement actually occurs. The
     * listener may return true from its {@link PlayerMovementListener#playerMove(Room, Room)}
     * method to halt further movement processing.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addBeforePlayerMovementListener(PlayerMovementListener l) {
        if (!beforePlayerMovementListeners.contains(l))
            beforePlayerMovementListeners.addFirst(l);
    }

    /**
     * Removes a PlayerMovementListener from the before-movement notification list.
     * @param l listener to remove
     */
    public void removeBeforePlayerMovementListener(PlayerMovementListener l) {
        beforePlayerMovementListeners.remove(l);
    }

    /**
     * Adds a PlayerMovementListener to be called before player movement actually occurs. The
     * listener may return true from its {@link PlayerMovementListener#playerMove(Room, Room)}
     * method to halt further movement processing.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addAfterPlayerMovementListener(PlayerMovementListener l) {
        if (!afterPlayerMovementListeners.contains(l))
            afterPlayerMovementListeners.addFirst(l);
    }

    /**
     * Removes a PlayerMovementListener from the after-movement notification list.
     * @param l listener to remove
     */
    public void removeAfterPlayerMovementListener(PlayerMovementListener l) {
        afterPlayerMovementListeners.remove(l);
    }

    /**
     * Notifies registered {@code PlayerMovementListener}S that the player is about to move.
     * @param from room player is moving from
     * @param to room player is moving to
     * @return true if any PlayerMovementListener interrupted the chain by returning true,
     *              and thus that player movement should be blocked.
     */
    private boolean fireBeforePlayerMovementEvent(Room from, Room to) {
        for (PlayerMovementListener l : beforePlayerMovementListeners) {
            if (l.playerMove(from, to))
                return true;
        }
        return false;
    }

    /**
     * Notifies register {@code PlayerMovementListener}S that the player has moved
     * @param from room player has moved from
     * @param to room player has moved to
     * @return true if any PlayerMovementListener interrupted the chain by returning true,
     */
    private boolean fireAfterPlayerMovementEvent(Room from, Room to) {
        for (PlayerMovementListener l : afterPlayerMovementListeners) {
            if (l.playerMove(from, to))
                return true;
        }
        return false;
    }

    /**
     * Adds a TurnListener to be notified when the turn begins.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addBeforeTurnListener(TurnListener l) {
        if (!beforeTurnListeners.contains(l))
            beforeTurnListeners.addFirst(l);
    }

    /**
     * Removes a TurnListener from the turn-beginning notification list.
     * @param l listener to remove
     */
    public void removeBeforeTurnListener(TurnListener l) {
        beforeTurnListeners.remove(l);
    }

    /**
     * Adds a TurnListener to be notified when the turn ends.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added listener will be
     * notified before previously added listeners.
     * @param l listener to add
     */
    public void addAfterTurnListener(TurnListener l) {
        if (!afterTurnListeners.contains(l))
            afterTurnListeners.addFirst(l);
    }

    /**
     * Removes a TurnListener from the turn-ending notification list.
     * @param l listener to remove
     */
    public void removeAfterTurnListener(TurnListener l) {
        afterTurnListeners.remove(l);
    }


    /** Notifies registered {@code TurnListener}S that a turn is beginning */
    private void fireBeforeTurnEvent() {
        for (TurnListener l : beforeTurnListeners)
            l.turn();
    }

    /** Notifies registered {@code TurnListener}S that a turn is ending */
    private void fireAfterTurnEvent() {
        for (TurnListener l : afterTurnListeners)
            l.turn();
    }

    /**
     * Adds a EntitySelectionListener.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addEntitySelectionListener(EntitySelectionListener l) {
        if (!entitySelectionListeners.contains(l))
            entitySelectionListeners.addFirst(l);
    }

    /**
     * Removes a EntitySelectionListener.
     * @param l listener to remove
     */
    public void removeEntitySelectionListener(EntitySelectionListener l) {
        entitySelectionListeners.remove(l);
    }

    /**
     * Notifies registered {@code EntitySelectionListener}S that an entity has been selected.
     * @param e selected entity
     * @param actions the mutable list of actions that should be shown in the UI,
     *      which each listener may modify.
     */
    private void fireEntitySelectedEvent(Entity e, List<String> actions) {
        for (EntitySelectionListener l : entitySelectionListeners)
            l.entitySelected(e, actions);
    }
    //endregion
}
