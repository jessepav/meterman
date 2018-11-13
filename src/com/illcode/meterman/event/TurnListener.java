package com.illcode.meterman.event;

/**
 * A TurnListener is notified at the end of each turn
 */
public interface TurnListener
{
    /** Called at the beginning of each turn, before any other processing begins, or at
     *  the end of the turn, after all other processing has finished. */
    void turn();
}
