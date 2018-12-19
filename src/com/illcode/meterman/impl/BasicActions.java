package com.illcode.meterman.impl;

import com.illcode.meterman.SystemActions;
import com.illcode.meterman.Utils;
import org.apache.commons.lang3.text.WordUtils;

public final class BasicActions
{
    public static String getExamineAction() {
        return Utils.getActionName("Examine");
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

    public static String getContainerExamineAction() {
        return Utils.getActionName("Examine Items");
    }

    public static String getContainerPutAction(String preposition) {
        return Utils.getActionName(WordUtils.capitalizeFully("Put Item " + preposition));
    }

    public static String getContainerTakeAction(String preposition) {
        return Utils.getActionName(WordUtils.capitalizeFully("Take Item " + preposition));
    }

    public static String getTakeAction() {
        return Utils.getActionName("Take");
    }

    public static String getPutAction() {
        return Utils.getActionName("Put");
    }
}
