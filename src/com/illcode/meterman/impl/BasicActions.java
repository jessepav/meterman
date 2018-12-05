package com.illcode.meterman.impl;

import com.illcode.meterman.SystemActions;
import com.illcode.meterman.Utils;

public class BasicActions extends SystemActions
{
    public static String getTakeAction() {
        return Utils.getActionName("Take");
    }

    public static String getDropAction() {
        return Utils.getActionName("Drop");
    }

    public static String getEquipAction() {
        return Utils.getActionName("Equip");
    }

    public static String getUnequipAction() {
        return Utils.getActionName("Unequip");
    }

    public static String getWearAction() {
        return Utils.getActionName("Wear");
    }

    public static String getTakeOffAction() {
        return Utils.getActionName("Take Off");
    }

    public static String getOpenAction() {
        return Utils.getActionName("Open");
    }

    public static String getCloseAction() {
        return Utils.getActionName("Close");
    }

    public static String getUnlockAction() {
        return Utils.getActionName("Unlock");
    }

    public static String getLockAction() {
        return Utils.getActionName("Lock");
    }

    public static String getTalkAction() {
        return Utils.getActionName("Talk to");
    }
}
