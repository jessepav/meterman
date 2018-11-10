package com.illcode.meterman;

public interface ClassMapper
{
    /**
     * Return the Room instance corresponding to an ID. There will be only one
     * instance of each type of room in the game world, so subsequent calls to
     * this method with the same ID will yield the same Room object.
     * @param id unique room ID
     * @return the Room instance corresponding to {@code id}
     */
    Room getRoom(String id);

    /**
     * Creates a new instance of an Entity whose class corresponds to a given ID.
     * @param id unique entity ID
     * @return a new Entity instance corresponding to {@code id}
     */
    Entity createEntity(String id);
}
