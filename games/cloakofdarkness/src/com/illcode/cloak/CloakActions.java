package com.illcode.cloak;

import com.illcode.meterman.Utils;
import com.illcode.meterman.impl.BasicActions;

public final class CloakActions
{
    public static String getHangOnHookAction() {
        return Utils.getActionName("Hang on Hook");
    }

    public static String getHangCloakAction() {
        return Utils.getActionName("Hang Cloak");
    }

    public static String getProdAction() {
        return Utils.getActionName("Prod");
    }

    public static String getPokeAction() {
        return Utils.getActionName("Poke");
    }

    public static String getKickAction() {
        return Utils.getActionName("Kick");
    }
}
