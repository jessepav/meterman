package com.illcode.cloak;

import java.util.Map;

public class CloakState
{
    public static final String CLOAK_STATE_KEY = "CloakState";

    public boolean cloakHung;  // is the cloak hung on the hook
    public int numDarkBarActions;  // how many actions the player attempted in the dark bar

    public void init() {
        cloakHung = false;
        numDarkBarActions = 0;
    }

    public void saveTo(Map<String,Object> worldData) {
        worldData.put(CLOAK_STATE_KEY, this);
    }

    public static CloakState retrieveFrom(Map<String,Object> worldData) {
        return (CloakState) worldData.get(CLOAK_STATE_KEY);
    }
}
