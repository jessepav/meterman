package com.illcode.meterman;

import com.illcode.meterman.event.GameActionListener;
import com.illcode.meterman.event.PlayerMovementListener;
import com.illcode.meterman.event.TurnListener;
import com.illcode.meterman.games.GamesList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class GameManager
{
    /** The game we're currently playing */
    private Game game;

    /** The ClassMapper used by the {@link #game} */
    private ClassMapper classMapper;

    /** The current state of the world */
    private WorldState worldState;

    // Elements of worldState, here for easy access
    private Player player;
    private List<Room> rooms;
    private Map<String,Object> data;

    // Our listener lists
    List<GameActionListener> gameActionListeners;
    List<PlayerMovementListener> playerMovementListeners;
    List<TurnListener> turnListeners;

    public GameManager() {
    }

    public void init() {
        gameActionListeners = new LinkedList<>();
        playerMovementListeners = new LinkedList<>();
        turnListeners = new LinkedList<>();
    }

    public void dispose() {
        game = null;
        classMapper = null;
        worldState = null;
        player = null;
        rooms = null;
        data = null;
        gameActionListeners = null;
        playerMovementListeners = null;
        turnListeners = null;
    }

    public void newGame(Game game) {
        this.game = game;
        worldState = game.getInitialWorldState();
        classMapper = game.getClassMapper();
        player = worldState.player;
        rooms = worldState.rooms;
        data = worldState.data;
    }

    public void loadGame(WorldState worldState) {
        this.worldState = worldState;
        game = GamesList.getGame(worldState.gameName);
        classMapper = game.getClassMapper();
        player = worldState.player;
        rooms = worldState.rooms;
        data = worldState.data;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Moves the player to a destination room. All appropriate listeners will be notified, and
     * one of them may cancel this move.
     * @param r the room to which the player should move.
     */
    public void movePlayer(Room r) {

    }

    /**
     * Moves an entity to a room. The entity can currently reside in a room, in player inventory,
     * or nowhere. This method calls {@link Entity#enterScope()} and {@link Entity#exitingScope()}
     * as needed.
     * @param e entity to move
     * @param r destination room, or null if the entity should be removed from the game world
     */
    public void moveEntity(Entity e, Room r) {

    }

    /**
     * Takes an entity (i.e. moves an entity to the player inventory). This method calls
     * {@link Entity#enterScope()} and {@link Entity#exitingScope()} as needed.
     * @param e entity to take
     * @return true if the take succeeded
     */
    public boolean takeEntity(Entity e) {
        return true;
    }

    /** Returns true if the given entity is in the player inventory. */
    public boolean entityInInventory(Entity e) {
        return player.inventory.contains(e);
    }

    /** Returns true if the entity is being worn by the player */
    public boolean entityWorn(Entity e) {
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
            if (e.checkAttribute(Attributes.WEARABLE) && entityInInventory(e) && !entityWorn(e)) {
                player.worn.add(e);
                return true;
            }
        } else {
            if (entityWorn(e)) {
                player.worn.remove(e);
                return true;
            }
        }
        return false;
    }

    /** Returns true if the entity is equipped by the player */
    public boolean entityEquipped(Entity e) {
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
            if (e.checkAttribute(Attributes.EQUIPPABLE) && entityInInventory(e) && !entityEquipped(e)) {
                player.equipped.add(e);
                return true;
            }
        } else {
            if (entityEquipped(e)) {
                player.equipped.remove(e);
                return true;
            }
        }
        return false;
    }

    /** @see ClassMapper#getRoom(String)  */
    public Room getRoom(String id) {
        return classMapper.getRoom(id);
    }

    /** @see ClassMapper#createEntity(String)  */
    public Entity createEntity(String id) {
        return classMapper.createEntity(id);
    }

    /** Called by the UI when the user clicks "Look" */
    public void lookAction() {

    }

    /** Called by the UI when the user clicks "Wait" */
    public void waitAction() {

    }

    /** Called by the UI when the user clicks an exit button */
    public void exitSelected(int direction) {

    }

    /** Called by the UI when the user clicks an action button (or selects an action
     *  from the combo box when there are many actions) */
    public void entityActionSelected(String action) {

    }

    public void aboutMenuClicked() {

    }

    /**
     * Called by the UI to indicate that the user selected an entity in the lists.
     * @param e entity selected, or null if no entity selected
     */
    public void entitySelected(Entity e) {

    }
}
