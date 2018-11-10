package com.illcode.meterman;

import com.illcode.meterman.ui.MetermanUI;
import com.illcode.meterman.ui.TinySoundManager;

public final class GameManager
{
    /** The game we're currently playing */
    private Game game;

    /** The current state of the world */
    private WorldState worldState;

    /** The ClassMapper used by the {@link #game} */
    private ClassMapper classMapper;

    /** The UI bound to this GameManager */
    public MetermanUI ui;

    /** The sound manager bound to this GameManager */
    public TinySoundManager sound;
}
