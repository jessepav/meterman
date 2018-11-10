package com.illcode.meterman.event;

/**
 * A TurnListener is notified at the end of each turn
 */
public interface TurnListener
{
    /** Called at the beginning of each turn, before the player has moved or
        performed an action, and before any other listeners are notified. */
    void turnBegin();
    
    /** Called at the end of each turn, after the player has moved or
        performed an action, and after all other listener types have been notified. */
    void turnEnd();
}
