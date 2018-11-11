package com.illcode.meterman;

/**
 * An instance of a particular game.
 */
public interface Game
{
    /** The name of the game, as displayed in the "New Game" UI */
    String getGameName();

    /** The ClassMapper used to instantiate entities and rooms used in this game */
    ClassMapper getClassMapper();

    /** The world state at the start of the game */
    WorldState getInitialWorldState();
}