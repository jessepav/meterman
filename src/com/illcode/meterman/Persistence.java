package com.illcode.meterman;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementations of this interface are responsible for serializing and deserializing
 * {@link WorldState} instances to and from streams.
 */
public interface Persistence
{
    /** Initialize the persistence implementation */
    void init();

    /** Dispose of any resources.*/
    void dispose();

    /**
     * Save a {@code WorldState} instance to an {@code OutputStream}.
     * @param state world-state instance
     * @param out output stream
     */
    void saveWorldState(WorldState state, OutputStream out);

    /**
     * Load a {@code WorldState} instance from an {@code InputStream}.
     * @param in input stream
     * @return new world-state instance
     */
    WorldState loadWorldState(InputStream in);
}
