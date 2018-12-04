package com.illcode.meterman.impl;

import com.illcode.meterman.SystemActions;
import com.illcode.meterman.Utils;

public class BasicActions extends SystemActions
{
    public static final String TAKE_ACTION = Utils.getActionName("Take");
    public static final String DROP_ACTION = Utils.getActionName("Drop");
    public static final String EQUIP_ACTION = Utils.getActionName("Equip");
    public static final String UNEQUIP_ACTION = Utils.getActionName("Unequip");
    public static final String WEAR_ACTION = Utils.getActionName("Wear");
    public static final String TAKEOFF_ACTION = Utils.getActionName("Take Off");
    public static final String OPEN_ACTION = Utils.getActionName("Open");
    public static final String CLOSE_ACTION = Utils.getActionName("Close");
    public static final String UNLOCK_ACTION = Utils.getActionName("Unlock");
    public static final String LOCK_ACTION = Utils.getActionName("Lock");
}
