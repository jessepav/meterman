package com.illcode.meterman;

import com.illcode.meterman.event.*;
import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.UIConstants;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.illcode.meterman.Meterman.ui;
import static com.illcode.meterman.Meterman.sound;

public final class GameManager
{
    /** The game we're currently playing */
    private Game game;

    /** The current state of the world */
    private WorldState worldState;

    // Elements of worldState, here for easy access
    private Player player;
    private Map<String,Object> worldData;

    // Our listener lists
    private LinkedList<GameActionListener> beforeGameActionListeners;
    private LinkedList<GameActionListener> defaultGameActionListeners;
    private LinkedList<PlayerMovementListener> beforePlayerMovementListeners;
    private LinkedList<PlayerMovementListener> afterPlayerMovementListeners;
    private LinkedList<TurnListener> turnListeners;
    private LinkedList<EntityActionsProcessor> entityActionsProcessors;
    private LinkedList<EntitySelectionListener> entitySelectionListeners;
    private LinkedList<DescriptionTextProcessor> descriptionTextProcessors;
    private LinkedList<ParserMessageProcessor> parserMessageProcessors;

    // To be used in composing text before sending it off to the UI.
    // Methods that use textBuilder should call .setLength(0) when finished to leave it empty.
    private StringBuilder textBuilder;

    // See queueLookText()
    private StringBuilder commonTextBuilder, paragraphBuilder;

    // Used for composing UI actions - reuse same list to avoid allocation
    private List<String> actions;

    private Entity selectedEntity;  // currently selected entity, or null if none

    private boolean alwaysLook; // see setAlwaysLook()

    public GameManager() {
    }

    public void init() {
        beforeGameActionListeners = new LinkedList<>();
        defaultGameActionListeners = new LinkedList<>();
        beforePlayerMovementListeners = new LinkedList<>();
        afterPlayerMovementListeners = new LinkedList<>();
        turnListeners = new LinkedList<>();
        entityActionsProcessors = new LinkedList<>();
        entitySelectionListeners = new LinkedList<>();
        descriptionTextProcessors = new LinkedList<>();
        parserMessageProcessors = new LinkedList<>();
        textBuilder = new StringBuilder(2048);
        commonTextBuilder = new StringBuilder(1024);
        paragraphBuilder = new StringBuilder(1024);
        actions = new ArrayList<>(16);
    }

    public void dispose() {
        closeGame();
        worldState = null;
        player = null;
        worldData = null;
        beforeGameActionListeners = null;
        defaultGameActionListeners = null;
        beforePlayerMovementListeners = null;
        afterPlayerMovementListeners = null;
        turnListeners = null;
        entityActionsProcessors = null;
        entitySelectionListeners = null;
        descriptionTextProcessors = null;
        parserMessageProcessors = null;
        textBuilder = null;
        commonTextBuilder = null;
        paragraphBuilder = null;
        actions = null;
    }

    /**
     * Start a new game.
     * Note that no listeners will be notified nor callbacks invoked at the start
     * of the first turn when the game begins; however, after the first look action, turn listeners
     * will be called.
     * @param game game to start
     */
    public void newGame(Game game) {
        closeGame();
        this.game = game;
        ui.setGameName(game.getGameName());
        game.init();
        worldState = game.getInitialWorldState();
        player = worldState.player;
        worldData = worldState.worldData;
        ensurePlayerInventoryConsistent();

        // We store our listener lists in the worldState so that they're persisted
        storeListenerListsInWorldData();
        ui.clearText();
        refreshRoomUI();
        refreshInventoryUI();
        entitySelected(null);
        ui.setFrameImage(MetermanUI.DEFAULT_FRAME_IMAGE);
        game.start(true);
        getCurrentRoom().entered(null);
        performLook(false);
        getCurrentRoom().setAttribute(Attributes.VISITED);
    }

    /** Make sure the player's inventory entities have integrity with the world. */
    private void ensurePlayerInventoryConsistent() {
        for (Entity e : player.worn)
            if (!player.inventory.contains(e))
                player.inventory.add(e);
        for (Entity e : player.equipped)
            if (!player.inventory.contains(e))
                player.inventory.add(e);
        for (Entity e : player.inventory)
            e.setRoom(player.currentRoom);
    }

    /**
     * Resume a game given a restored WorldState. While the world model graph will
     * be as it was when the game was saved, the UI may be slightly different (no images,
     * and definitely no scrollback in the main text area).
     * @param worldState WorldState to restore
     */
    public void loadGame(WorldState worldState) {
        closeGame();
        this.worldState = worldState;
        game = GamesList.getGame(worldState.gameName);
        ui.setGameName(game.getGameName());
        game.init();
        player = worldState.player;
        worldData = worldState.worldData;

        restoreListenerListsFromWorldData();
        ui.clearText();
        refreshRoomUI();
        refreshInventoryUI();
        entitySelected(null);
        ui.setFrameImage(MetermanUI.DEFAULT_FRAME_IMAGE);
        game.start(false);
    }

    private void closeGame() {
        ui.unloadAllImages();
        sound.clearAudio();
        clearListenerLists();
        player = null;
        worldData = null;
        worldState = null;
        Meterman.setGameBundle(null);
        Utils.resetActionNameTranslations();
        Utils.setGameAssetsPath(null);
        if (game != null) {
            game.dispose();
            game = null;
        }
        ui.setGameName(null);
    }

    private void storeListenerListsInWorldData() {
        worldData.put("beforeGameActionListeners", beforeGameActionListeners);
        worldData.put("defaultGameActionListeners", defaultGameActionListeners);
        worldData.put("beforePlayerMovementListeners", beforePlayerMovementListeners);
        worldData.put("afterPlayerMovementListeners", afterPlayerMovementListeners);
        worldData.put("turnListeners", turnListeners);
        worldData.put("entityActionsProcessors", entityActionsProcessors);
        worldData.put("entitySelectionListeners", entitySelectionListeners);
        worldData.put("descriptionTextProcessors", descriptionTextProcessors);
        worldData.put("parserMessageProcessors", parserMessageProcessors);
    }

    @SuppressWarnings("unchecked")
    private void restoreListenerListsFromWorldData() {
        beforeGameActionListeners = (LinkedList<GameActionListener>) worldData.get("beforeGameActionListeners");
        defaultGameActionListeners = (LinkedList<GameActionListener>) worldData.get("defaultGameActionListeners");
        beforePlayerMovementListeners = (LinkedList<PlayerMovementListener>) worldData.get("beforePlayerMovementListeners");
        afterPlayerMovementListeners = (LinkedList<PlayerMovementListener>) worldData.get("afterPlayerMovementListeners");
        turnListeners = (LinkedList<TurnListener>) worldData.get("turnListeners");
        entityActionsProcessors = (LinkedList<EntityActionsProcessor>) worldData.get("entityActionsProcessors");
        entitySelectionListeners = (LinkedList<EntitySelectionListener>) worldData.get("entitySelectionListeners");
        descriptionTextProcessors = (LinkedList<DescriptionTextProcessor>) worldData.get("descriptionTextProcessors");
        parserMessageProcessors = (LinkedList<ParserMessageProcessor>) worldData.get("parserMessageProcessors");
    }

    private void clearListenerLists() {
        beforeGameActionListeners.clear();
        defaultGameActionListeners.clear();
        beforePlayerMovementListeners.clear();
        afterPlayerMovementListeners.clear();
        turnListeners.clear();
        entityActionsProcessors.clear();
        entitySelectionListeners.clear();
        descriptionTextProcessors.clear();
        parserMessageProcessors.clear();
    }

    public Game getGame() {
        return game;
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

    public Map<String,Object> getWorldData() {
        return worldData;
    }

    public int getNumTurns() {
        return worldState.numTurns;
    }

    /**
     * Moves the player to a destination room. All appropriate listeners will be notified, and
     * one of them may cancel this move.
     * @param toRoom the room to which the player should move.
     */
    public void movePlayer(Room toRoom) {
        if (toRoom == null || toRoom == player.currentRoom)
            return;
        Room fromRoom = player.currentRoom;
        // Here we go...
        if (fireBeforePlayerMovement(fromRoom, toRoom))
            return;  // we were blocked by a listener
        if (fromRoom.exiting(toRoom))
            return;  // blocked by the room itself
        for (Entity e : fromRoom.getRoomEntities())
            e.exitingScope();
        player.currentRoom = toRoom;
        toRoom.entered(fromRoom);
        for (Entity e : player.inventory)
            e.setRoom(toRoom);
        for (Entity e : toRoom.getRoomEntities())
            e.enterScope();
        fireAfterPlayerMovement(fromRoom, toRoom);
        ui.clearEntitySelection();  // this in turn will call entitySelected(null) if needed
        if (alwaysLook || !toRoom.checkAttribute(Attributes.VISITED))
            performLook(false);
        toRoom.setAttribute(Attributes.VISITED);
        refreshRoomUI();
    }

    /**
     * Moves an entity to a room. The entity can currently reside in a room, in player inventory,
     * or nowhere.
     * @param e entity to move
     * @param toRoom destination room, or null if the entity should be removed from the game world
     */
    public void moveEntity(Entity e, Room toRoom) {
        Room previousRoom = e.getRoom();
        Room playerRoom = player.currentRoom;

        // First, let's deal with where the entity is coming from
        if (isInInventory(e)) {
            player.worn.remove(e);
            player.equipped.remove(e);
            player.inventory.remove(e);
            e.dropped();
            refreshInventoryUI();
        } else if (previousRoom != null) {
            previousRoom.getRoomEntities().remove(e);
        }

        // If the entity is changing rooms, we need to deal with scope issues
        if (previousRoom != toRoom) {
            if (previousRoom == playerRoom)
                e.exitingScope();
            e.setRoom(toRoom);
            if (toRoom == playerRoom)
                e.enterScope();
        }

        // If the entity is being moved to a room, as opposed to nowhere, add it to that room
        if (toRoom != null)
            toRoom.getRoomEntities().add(e);

        // And we may need to update our UI and selection
        if (previousRoom == playerRoom || toRoom == playerRoom)
            refreshRoomUI();
        if (e == selectedEntity)
            entitySelected(null);
    }

    /**
     * Takes an entity (i.e. moves an entity to the player inventory).
     * @param e entity to take
     */
    public void takeEntity(Entity e) {
        if (!isInInventory(e)) {
            Room previousRoom = e.getRoom();
            Room playerRoom = player.currentRoom;
            if (previousRoom != null)
                previousRoom.getRoomEntities().remove(e);
            player.inventory.add(e);
            e.taken();
            if (previousRoom != playerRoom) {
                e.setRoom(playerRoom);
                e.enterScope();
            } else {
                refreshRoomUI();
            }
            refreshInventoryUI();
        }
    }

    /** Returns true if the given entity is in the player inventory. */
    public boolean isInInventory(Entity e) {
        return player.inventory.contains(e);
    }

    /** Returns true if the entity is being worn by the player */
    public boolean isWorn(Entity e) {
        return player.worn.contains(e);
    }

    /**
     * Sets whether an entity is worn by the player.
     * @param e entity to wear
     * @param wear whether the player should wear the entity. If true, and the given entity is in
     *      the player inventory and is {@link Attributes#WEARABLE wearable}, the player will wear it.
     * @return true if the operation succeeded
     */
    public boolean setWorn(Entity e, boolean wear) {
        if (wear) {
            if (e.checkAttribute(Attributes.WEARABLE) && isInInventory(e) && !isWorn(e)) {
                player.worn.add(e);
                refreshInventoryUI();
                return true;
            }
        } else {
            if (isWorn(e)) {
                player.worn.remove(e);
                refreshInventoryUI();
                return true;
            }
        }
        return false;
    }

    /** Returns true if the entity is equipped by the player */
    public boolean isEquipped(Entity e) {
        return player.equipped.contains(e);
    }

    /**
     * Sets whether an entity is equipped by the player.
     * @param e entity to equip
     * @param equip whether the player should equip the entity. If true, and the given entity is in
     *      the player inventory and is {@link Attributes#EQUIPPABLE equippable}, the player will equip it.
     * @return true if the operation succeeded
     */
    public boolean setEquipped(Entity e, boolean equip) {
        if (equip) {
            if (e.checkAttribute(Attributes.EQUIPPABLE) && isInInventory(e) && !isEquipped(e)) {
                player.equipped.add(e);
                refreshInventoryUI();
                return true;
            }
        } else {
            if (isEquipped(e)) {
                player.equipped.remove(e);
                refreshInventoryUI();
                return true;
            }
        }
        return false;
    }

    /** Called by the UI when the user selects the "About..." menu item.*/
    public void aboutMenuClicked() {
        ui.appendNewline();
        ui.appendTextLn("> " + SystemActions.getAboutAction().toUpperCase());
        game.about();
    }

    /** Called by the UI when the user clicks "Look", or when the player moves rooms */
    public void lookCommand() {
        performLook(true);
        nextTurn();
    }

    /**
     * Actually performs the look command, optionally showing a parser-like prompt message.
     * @param showParserMessage true to show parser-like message
     */
    private void performLook(boolean showParserMessage) {
        if (showParserMessage) {
            ui.appendNewline();
            ui.appendTextLn("> " + SystemActions.getLookAction().toUpperCase());
        }
        textBuilder.append(getCurrentRoom().getDescription());
        textBuilder.append("\n");
        for (Entity e : getCurrentRoom().getRoomEntities())
            e.lookInRoom();
        if (commonTextBuilder.length() != 0) {
            textBuilder.append('\n').append(commonTextBuilder).append('\n');
            commonTextBuilder.setLength(0);
        }
        if (paragraphBuilder.length() != 0) {
            textBuilder.append(paragraphBuilder);
            paragraphBuilder.setLength(0);
        }
        fireDescriptionTextReady(textBuilder, DescriptionTextProcessor.ROOM_DESCRIPTION);
        ui.appendText(textBuilder.toString());
        textBuilder.setLength(0);
    }

    /**
     * Adds text to be shown during the next look action.
     * @param text text to show
     * @param paragraph if true, the text will be shown in its own paragraph; if false, it will
     *          be shown along with any other text that didn't request a separate paragraph
     */
    public void queueLookText(String text, boolean paragraph) {
        if (paragraph)
            paragraphBuilder.append('\n').append(text).append('\n');
        else
            commonTextBuilder.append(text).append(' ');
    }

    /** Called by the UI when the user clicks "Wait" */
    public void waitCommand() {
        ui.appendNewline();
        ui.appendTextLn("> " + SystemActions.getWaitAction().toUpperCase());
        ui.appendTextLn(Meterman.getSystemBundle().getPassage("wait-message"));
        nextTurn();
    }

    /** Called as one turn is transitioning to the next (before {@link WorldState#numTurns} is incremented) */
    private void nextTurn() {
        fireTurn();
        worldState.numTurns++;
    }
    
    /** Called by the UI when the user clicks an exit button */
    public void exitSelected(int position) {
        Room toRoom = getCurrentRoom().getExit(position);
        if (toRoom != null) {
            // "> GO TO <exit label>"
            ui.appendNewline();
            ui.appendTextLn(Utils.fmt("> %s %s",
                SystemActions.getGoAction().toUpperCase(), toRoom.getExitName().toUpperCase()));
            movePlayer(toRoom);
        }
        nextTurn();
    }

    /** Called by the UI when the user clicks an action button (or selects an action
     *  from the combo box when there are many actions) */
    public void entityActionSelected(String action) {
        String msg = selectedEntity.replaceParserMessage(action);
        if (msg == null)
            msg = fireProcessingParserMessage(selectedEntity, action);
        if (msg == null)
            msg = Utils.fmt("> %s %s", action.toUpperCase(), selectedEntity.getName().toUpperCase());
        if (!msg.isEmpty()) {
            ui.appendNewline();
            ui.appendTextLn(msg);
        }
        if (!fireBeforeAction(action, selectedEntity)) {
            if (!selectedEntity.processAction(action))
                if (!fireDefaultAction(action, selectedEntity))
                    ui.appendTextLn(Meterman.getSystemBundle().getPassage("action-not-handled"));
        }
        nextTurn();
    }

    /**
     * Called by the UI to indicate that the user selected an entity in the lists.
     * @param e entity selected, or null if no entity selected
     */
    public void entitySelected(Entity e) {
        selectedEntity = e;
        refreshEntityUI();
        if (e != null) {
            if (!e.selected())
                fireEntitySelected(e);
        }
    }

    /**
     * May be called to indicate that the given entity's state has changed in such
     * a way that the UI may need to be refreshed.
     * @param e entity that has changed
     */
    public void entityChanged(Entity e) {
        if (e != null && e == selectedEntity)
            refreshEntityUI();
    }

    private void refreshEntityUI() {
        if (selectedEntity != null) {
            actions.clear();
            actions.addAll(selectedEntity.getActions());
            fireProcessEntityActions(selectedEntity, actions);
            ui.setObjectName(selectedEntity.getName());
            textBuilder.append(selectedEntity.getDescription());
            fireDescriptionTextReady(textBuilder, DescriptionTextProcessor.ENTITY_DESCRIPTION);
            ui.setObjectText(textBuilder.toString());
            textBuilder.setLength(0);
            ui.clearActions();
            for (String s : actions)
                ui.addAction(s);
        } else {
            ui.clearActions();
            ui.setObjectName("(nothing selected)");
            ui.setObjectText("");
            ui.setEntityImage(MetermanUI.NO_IMAGE);
        }
    }

    /**
     * Called when a room's internal state changes in such a way that the UI
     * may have to be updated to reflect the change.
     * @param r room that has changed
     */
    public void roomChanged(Room r) {
        if (r == getCurrentRoom())
            refreshRoomUI();
    }

    private void refreshRoomUI() {
        Room r = getCurrentRoom();
        ui.setRoomName(r.getName());
        for (int pos = 0; pos < UIConstants.NUM_EXIT_BUTTONS; pos++)
            ui.setExitLabel(pos, r.getExitLabel(pos));
        ui.clearRoomEntities();
        for (Entity e : r.getRoomEntities())
            if (!e.checkAttribute(Attributes.CONCEALED))
                ui.addRoomEntity(e);
    }

    /**
     * Called when the player inventory changes in such a way that the UI needs to be refreshed.
     */
    private void refreshInventoryUI() {
        HashSet<Entity> remainingItems = new HashSet<>(player.inventory);
        Entity savedSE = selectedEntity;
        ui.clearInventoryEntities();
        for (Entity item : player.equipped) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item, "(e)");
        }
        for (Entity item : player.worn) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item, "(w)");
        }
        for (Entity item : player.inventory) {
            if (remainingItems.remove(item))
                ui.addInventoryEntity(item, null);
        }
        if (isInInventory(savedSE))
            ui.selectEntity(savedSE);
    }

    /** Called by the UI when it's time to load a saved game*/
    public void loadGameState(InputStream in) {
        loadGame(Meterman.persistence.loadWorldState(in));
        ui.appendText("\n------- Game Loaded -------\n\n");
    }

    /** Called by the UI when it's time to save a game*/
    public void saveGameState(OutputStream out) {
        Meterman.persistence.saveWorldState(worldState, out);
        ui.appendText("\n------- Game Saved -------\n\n");
    }

    /** Set whether we should always "Look" when entering a room, even if it's
     *  been visited before. */
    public void setAlwaysLook(boolean alwaysLook) {
        this.alwaysLook = alwaysLook;
    }

    /** @see #setAlwaysLook(boolean) */
    public boolean isAlwaysLook() {
        return alwaysLook;
    }

    //region Event Listener methods
    /**
     * Adds a GameActionListener to be called before a game action is processed. If the listener's
     * {@link GameActionListener#processAction(String, Entity, boolean)} method returns true, further action
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
    private boolean fireBeforeAction(String action, Entity e) {
        for (GameActionListener l : beforeGameActionListeners) {
            if (l.processAction(action, e, true))
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
    private boolean fireDefaultAction(String action, Entity e) {
        for (GameActionListener l : defaultGameActionListeners) {
            if (l.processAction(action, e, false))
                return true;
        }
        return false;
    }

    /**
     * Adds a PlayerMovementListener to be called before player movement actually occurs. The
     * listener may return true from its {@link PlayerMovementListener#playerMove}
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
     * listener may return true from its {@link PlayerMovementListener#playerMove}
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
    private boolean fireBeforePlayerMovement(Room from, Room to) {
        for (PlayerMovementListener l : beforePlayerMovementListeners) {
            if (l.playerMove(from, to, true))
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
    private boolean fireAfterPlayerMovement(Room from, Room to) {
        for (PlayerMovementListener l : afterPlayerMovementListeners) {
            if (l.playerMove(from, to, false))
                return true;
        }
        return false;
    }

    /**
     * Adds a TurnListener to be notified when the turn cycles to the next.
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
     * Removes a TurnListener from the turn-cycle notification list.
     * @param l listener to remove
     */
    public void removeTurnListener(TurnListener l) {
        turnListeners.remove(l);
    }

    /** Notifies registered {@code TurnListener}S that we have reached the cycle of turns */
    private void fireTurn() {
        for (TurnListener l : turnListeners)
            l.turn();
    }

    /**
     * Adds an EntityActionsProcessor.
     * <p/>
     * Listeners are added to the front of our list, and thus the most recently added
     * listener will be notified before previously added listeners.
     * @param l listener to add
     */
    public void addEntityActionsProcessor(EntityActionsProcessor l) {
        if (!entityActionsProcessors.contains(l))
            entityActionsProcessors.addFirst(l);
    }

    /**
     * Removes a EntityActionsProcessor.
     * @param l listener to remove
     */
    public void removeEntityActionsProcessor(EntityActionsProcessor l) {
        entityActionsProcessors.remove(l);
    }

    /**
     * Notifies registered {@code EntityActionsProcessor}S that an entity's action list is
     * being generated.
     * @param e entity
     * @param actions the mutable list of actions that should be shown in the UI,
     *      which each listener may modify.
     */
    private void fireProcessEntityActions(Entity e, List<String> actions) {
        for (EntityActionsProcessor l : entityActionsProcessors)
            l.processEntityActions(e, actions);
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
     */
    private void fireEntitySelected(Entity e) {
        for (EntitySelectionListener l : entitySelectionListeners)
            if (l.entitySelected(e))
                break;
    }

    /**
     * Add a DescriptionTextProcessor
     * @param l listener to add
     */
    public void addDescriptionTextProcessor(DescriptionTextProcessor l) {
        if (!descriptionTextProcessors.contains(l))
            descriptionTextProcessors.addFirst(l);
    }

    /**
     * Remove a DescriptionTextProcessor
     * @param l listener to remove
     */
    public void removeDescriptionTextProcessor(DescriptionTextProcessor l) {
        descriptionTextProcessors.remove(l);
    }

    /**
     * Notifies registered <tt>DescriptionTextProcessor</tt>s that the text of a look command has been
     * gathered and will be displayed in the UI.
     * @param sb the StringBuilder containing the text to be shown
     * @param textType either {@link DescriptionTextProcessor#ROOM_DESCRIPTION} or {@link
     *   DescriptionTextProcessor#ENTITY_DESCRIPTION}, indicating at what point the method is being called.
     */
    private void fireDescriptionTextReady(StringBuilder sb, int textType) {
        for (DescriptionTextProcessor l : descriptionTextProcessors)
            l.descriptionTextReady(sb, textType);
    }

    /**
     * Add a ParserMessageProcessor
     * @param p processor to add
     */
    public void addParserMessageProcessor(ParserMessageProcessor p) {
        if (!parserMessageProcessors.contains(p))
            parserMessageProcessors.addFirst(p);
    }

    /**
     * Remove a ParserMessageProcessor
     * @param p processor to remove
     */
    public void removeParserMessageProcessor(ParserMessageProcessor p) {
        parserMessageProcessors.remove(p);
    }

    /**
     * Called when a parser message is being generated to notify all our registered
     * <tt>ParserMessageProcessor</tt>S.
     * @param e selected entity performing the action
     * @param action the action that is being performed
     * @return null to allow the normal parser message flow to continue, <tt>""</tt> to suppress the
     *         parser message entirely, or a non-empty string to replace the default parser message.
     */
    private String fireProcessingParserMessage(Entity e, String action) {
        String msg = null;
        for (ParserMessageProcessor p : parserMessageProcessors) {
            msg = p.replaceParserMessage(e, action);
            if (msg != null)
                break;
        }
        return msg;
    }
    //endregion
}
