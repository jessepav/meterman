package com.illcode.meterman;

public final class SystemActions
{
    public static String getGoAction() {
        return Utils.getActionName("Go to");
    }

    public static String getLookAction() {
        return Utils.getActionName("Look");
    }

    public static String getWaitAction() {
        return Utils.getActionName("Wait");
    }

    public static String getAboutAction() {
        return Utils.getActionName("About");
    }
}
