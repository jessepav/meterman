package com.illcode.meterman;

import com.illcode.meterman.event.EntitySelectionListener;
import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.games.GamesList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private LinkedList<GameActionListener> gameActionListeners;
    private LinkedList<PlayerMovementListener> playerMovementListeners;
    private LinkedList<TurnListener> turnListeners;
    private LinkedList<EntitySelectionListener> entitySelectionListeners;

    // To be used in composing text before sending it off to the UI
    private StringBuilder sb;

    // Used for composing UI actions - reuse same list to avoid allocation
    private List<String> actions;

    private Entity selectedEntity;  // currently selected entity, or null if none

    public GameManager() {
    }

    public void init() {
        gameActionListeners = new LinkedList<>();
        playerMovementListeners = new LinkedList<>();
        turnListeners = new LinkedList<>();
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
        gameActionListeners = null;
        playerMovementListeners = null;
        turnListeners = null;
        entitySelectionListeners = null;
        sb = null;
        actions = null;
    }

    public void newGame(Game game) {
        this.game = game;
        worldState = game.getInitialWorldState();
        player = worldState.player;
        rooms = worldState.rooms;
        worldData = worldState.worldData;
    }

    public void loadGame(WorldState worldState) {
        this.worldState = worldState;
        game = GamesList.getGame(worldState.gameName);
        player = worldState.player;
        rooms = worldState.rooms;
        worldData = worldState.worldData;
    }

    public Player getPlayer() {
        return player;
    }

    public Room getCurrentRoom() {
        return player.currentRoom;
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    /**
     * Moves the player to a destination room. All appropriate listeners will be notified, and
     * one of them may cancel this move.
     * @param room the room to which the player should move.
     */
    public void movePlayer(Room room) {
        if (room == player.currentRoom) // we're already there
            return;
    }

    /**
     * Moves an entity to a room. The entity can currently reside in a room, in player inventory,
     * or nowhere.
     * @param e entity to move
     * @param r destination room, or null if the entity should be removed from the game world
     */
    public void moveEntity(Entity e, Room r) {

    }

    /**
     * Takes an entity (i.e. moves an entity to the player inventory).
     * @param e entity to take
     */
    public void takeEntity(Entity e) {
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
    }

    /** Called by the UI when the user clicks "Wait" */
    public void waitAction() {
        fireTurnEndEvent();
        fireTurnBeginEvent();
    }

    /** Called by the UI when the user clicks an exit button */
    public void exitSelected(int direction) {
        movePlayer(getCurrentRoom().getExit(direction));
    }

    /** Called by the UI when the user clicks an action button (or selects an action
     *  from the combo box when there are many actions) */
    public void entityActionSelected(String action) {
        if (!fireBeforeActionEvent(action, selectedEntity)) {
            if (!selectedEntity.processAction(action))
                defaultProcessAction(selectedEntity, action);
        }
        fireAfterActionEvent(action, selectedEntity);
    }

    /**
     * Called if neither {@code GameActionListener}S nor the entity itself handled an action.
     * @param e selected entity
     * @param action action name
     */
    private void defaultProcessAction(Entity e, String action) {
        // TODO: ActionProcessor interface, DefaultActionProcessor class
    }

    /**
     * Called by the UI to indicate that the user selected an entity in the lists.
     * @param e entity selected, or null if no entity selected
     */
    public void entitySelected(Entity e) {
        selectedEntity = e;
        ui.clearActions();
        if (e == null) {
            ui.setObjectName("(nothing selected)");
            ui.setObjectText("");
            ui.setEntityImage(null);
        } else {
            e.selected();
            actions.clear();
            actions.addAll(e.getActions());
            fireEntitySelectedEvent(e, actions);
            for (String s : actions)
                ui.addAction(s);
        }
    }

    /**
     * Adds a GameActionListener.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addGameActionListener(GameActionListener l) {
        if (!gameActionListeners.contains(l))
            gameActionListeners.addFirst(l);
    }

    /**
     * Removes a GameActionListener.
     * @param l listener to remove
     */
    public void removeGameActionListener(GameActionListener l) {
        gameActionListeners.remove(l);
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
        for (GameActionListener l : gameActionListeners) {
            if (l.beforeAction(action, e))
                return true;
        }
        return false;
    }

    /**
     * Notifies registered {@code GameActionListener}S that an action has been processed
     * @param action action name
     * @param e selected entity
     */
    private void fireAfterActionEvent(String action, Entity e) {
        for (GameActionListener l : gameActionListeners)
            l.afterAction(action, e);
    }

    /**
     * Adds a PlayerMovementListener.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addPlayerMovementListener(PlayerMovementListener l) {
        if (!playerMovementListeners.contains(l))
            playerMovementListeners.addFirst(l);
    }

    /**
     * Removes a PlayerMovementListener.
     * @param l listener to remove
     */
    public void removePlayerMovementListener(PlayerMovementListener l) {
        playerMovementListeners.remove(l);
    }

    /**
     * Notifies registered {@code PlayerMovementListener}S that the player is about to move.
     * @param from room player is moving from
     * @param to room player is moving to
     * @return true if any PlayerMovementListener interrupted the chain by returning true,
     *              and thus that player movement should be blocked.
     */
    private boolean firePlayerMovingEvent(Room from, Room to) {
        for (PlayerMovementListener l : playerMovementListeners) {
            if (l.playerMoving(from, to))
                return true;
        }
        return false;
    }

    /**
     * Notifies register {@code PlayerMovementListener}S that the player has moved
     * @param from room player has moved from
     * @param to room player has moved to
     */
    private void firePlayerMovedEvent(Room from, Room to) {
        for (PlayerMovementListener l : playerMovementListeners)
            l.playerMoved(from, to);
    }

    /**
     * Adds a TurnListener.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addTurnListener(TurnListener l) {
        if (!turnListeners.contains(l))
            turnListeners.addFirst(l);
    }

    /**
     * Removes a TurnListener.
     * @param l listener to remove
     */
    public void removeTurnListener(TurnListener l) {
        turnListeners.remove(l);
    }

    /** Notifies registered {@code TurnListener}S that a turn is beginning */
    private void fireTurnBeginEvent() {
        for (TurnListener l : turnListeners)
            l.turnBegin();
    }

    /** Notifies registered {@code TurnListener}S that a turn is ending */
    private void fireTurnEndEvent() {
        for (TurnListener l : turnListeners)
            l.turnEnd();
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
}
