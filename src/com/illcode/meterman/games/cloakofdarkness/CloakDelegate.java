package com.illcode.meterman.games.cloakofdarkness;

import com.illcode.meterman.impl.*;

import java.util.List;
import java.util.Map;


public class CloakDelegate extends EntityDelegateAdapter implements RoomDelegate
{
    Map<String,BaseEntity> entityIdMap;
    Map<String,BaseRoom> roomIdMap;

    void init(Map<String,BaseEntity> entityIdMap, Map<String,BaseRoom> roomIdMap) {
        this.entityIdMap = entityIdMap;
        this.roomIdMap = roomIdMap;
    }

    //region -- EntityDelegate --

    public List<String> getActions(BaseEntity e) {
        return super.getActions(e);
    }

    public boolean processAction(BaseEntity e, String action) {
        return super.processAction(e, action);
    }

    //endregion

    //region -- RoomDelegate --

    public String getDescription(BaseRoom r) {
        return null;
    }

    public void entered(BaseRoom r) {

    }

    public void exiting(BaseRoom r) {

    }
    //endregion
}
