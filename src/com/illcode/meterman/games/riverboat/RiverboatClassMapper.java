package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.ClassMapper;
import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

import java.util.HashMap;
import java.util.Map;

public class RiverboatClassMapper implements ClassMapper
{
    Map<String,Room> roomMap;

    public RiverboatClassMapper() {
        roomMap = new HashMap<>(100);
    }

    public Room getRoom(String id) {
        return null;
    }

    public Entity createEntity(String id) {
        return null;
    }
}
