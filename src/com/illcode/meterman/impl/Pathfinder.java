package com.illcode.meterman.impl;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;
import com.illcode.meterman.ui.UIConstants;

import java.util.*;

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

    /**
     * Attempts to find a path between two rooms.
     * <p/>
     * Since in our world model it is rather impossible to make estimations as to the "cost" between
     * two rooms, we just do a simple breadth-first-search (BFS).
     * @param start room from which to start pathfinding
     * @param destination room to which we're attempting to find a path
     * @return a list of rooms that is the path from start to goal, not including
     *          the start tile, or null if no path found.
     */
    public List<Room> findPath(Room start, Room destination) {
        openQueue.add(new PathNode(start, null));
        Set<PathNode> neighbors = new HashSet<>();
        try {
            while(!openQueue.isEmpty()) {
                PathNode currentNode = openQueue.remove();
                if (currentNode.room == destination)
                    return constructPath(currentNode);
                closedSet.add(currentNode);
                neighbors.clear();
                gatherNeighbors(currentNode, neighbors);
                for (PathNode neighbor : neighbors) {
                    if (!openQueue.contains(neighbor) && !closedSet.contains(neighbor))
                        openQueue.add(neighbor);
                }
            }
            return null;  // no path found
        } finally {
            openQueue.clear();
            closedSet.clear();
        }
    }

    private void gatherNeighbors(PathNode currentNode, Set<PathNode> neighbors) {
        // First add all the normal exit neighbors of the room
        Room currentRoom = currentNode.room;
        for (int direction = 0; direction < UIConstants.NUM_EXIT_BUTTONS; direction++) {
            Room r = currentRoom.getExit(direction);
            if (r != null)
                neighbors.add(new PathNode(r, currentNode));
        }
        // Then check for closed, unlocked doors.
        for (Entity e : currentRoom.getRoomEntities()) {
            if (e instanceof Door) {
                Door d = (Door) e;
                if (!d.isOpen() && !d.isLocked()) {
                    Room r1 = d.getRoom(0);
                    Room r2 = d.getRoom(1);
                    if (r1 == currentRoom)
                        neighbors.add(new PathNode(r2, currentNode));
                    else if (r2 == currentRoom)
                        neighbors.add(new PathNode(r1, currentNode));
                }
            }
        }
    }

    private List<Room> constructPath(PathNode node) {
        LinkedList<Room> path = new LinkedList<>();
        while (node.parent != null) {
            path.addFirst(node.room);
            node = node.parent;
        }
        return path;
    }

    private static class PathNode
    {
        Room room;
        PathNode parent;

        private PathNode(Room room, PathNode parent) {
            this.room = room;
            this.parent = parent;
        }

        public boolean equals(Object obj) {
            return room.equals(obj);
        }

        public int hashCode() {
            return room.hashCode();
        }
    }
}
