package com.illcode.meterman.impl;

import com.illcode.meterman.Room;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Finds the shortest path between two rooms, if one exists, taking into account unlocked {@link Door}S.
 */
public class Pathfinder
{
    private Queue<PathNode> openQueue;
    private HashSet<PathNode> closedSet;

    public Pathfinder() {
        openQueue = new LinkedList<>();
        closedSet = new HashSet<>();
    }

    private static class PathNode
    {
        Room r;
        PathNode parent;

        private PathNode(Room r, PathNode parent) {
            this.r = r;
            this.parent = parent;
        }

        public boolean equals(Object obj) {
            return r.equals(obj);
        }

        public int hashCode() {
            return r.hashCode();
        }
    }
}
