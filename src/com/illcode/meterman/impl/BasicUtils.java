package com.illcode.meterman.impl;

import com.illcode.meterman.Meterman;
import com.illcode.meterman.Room;

import static com.illcode.meterman.Meterman.gm;
import static com.illcode.meterman.Meterman.ui;

/**
 * Utility methods that operate on the various classes in <tt>com.illcode.meterman.impl</tt>.
 */
public class BasicUtils
{
    /**
     * Ends the game by taking an undo checkpoint and then transporting the player to
     * an empty room with no exits or objects.
     * @param message message to display before transporting the player
     */
    public static void endGame(String message) {
        WorldBuilder wb = new WorldBuilder(gm.getWorldState(), Meterman.getSystemBundle());
        BaseRoom r = wb.loadRoom("default-endgame-room");
        endGame(message, r);
    }

    /**
     * Ends the game by taking an undo checkpoint and then transporting the player to a given room.
     * @param message message to display before transporting the player
     * @param r room where the player will be transported
     */
    public static void endGame(String message, Room r) {
        gm.undoCheckpoint();
        ui.appendTextLn(message);
        ui.appendNewline();
        gm.movePlayer(r);
    }
}
