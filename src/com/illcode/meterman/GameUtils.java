package com.illcode.meterman;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods that apply to the game world
 */
public class GameUtils
{
    /**
     * Returns the subset of a given list of entities that have a specific attribute value.
     * @param entities list of entities to filter
     * @param attribute attribute to filter by
     * @param value the value of the attribute
     * @return a list of entities that have {@code attribute = value}
     */
    public static List<Entity> filterByAttribute(List<Entity> entities, int attribute, boolean value) {
        List<Entity> filteredList = new LinkedList<>();
        for (Entity e : entities) {
            if (e.checkAttribute(attribute) == value)
                filteredList.add(e);
        }
        return filteredList;
    }
}
