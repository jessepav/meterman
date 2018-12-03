package com.illcode.meterman;

import com.illcode.meterman.ui.MetermanUI;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods that apply to the game world or interface.
 */
public class GameUtils
{
    private static String defaultMoreLabel;
    private static String defaultCloseLabel;

    /**
     * Returns the subset of a given list of entities that have a specific attribute value.
     * @param entities list of entities to filter
     * @param attribute attribute to filter by
     * @param value the value of the attribute
     * @return a list of entities that have {@code attribute = value}
     */
    public static List<Entity> filterByAttribute(List<Entity> entities, int attribute, boolean value) {
        List<Entity> filteredList = new LinkedList<>();
        for (Entity e : entities) {
            if (e.checkAttribute(attribute) == value)
                filteredList.add(e);
        }
        return filteredList;
    }

    /**
     * Show a sequence of passages in via {@link MetermanUI#showTextDialog(String, String, String)}.
     * @param b text bundle to use
     * @param header header label
     * @param moreLabel the button label to show on all but the last passage
     * @param closeLabel the button label to show on the last passage
     * @param passageNames the bundle passages to show
     */
    public static void showPassages(TextBundle b, String header,
                    String moreLabel, String closeLabel, String... passageNames) {
        int n = passageNames.length;
        for (int i = 0; i < n; i++) {
            String label = (i == n - 1) ? closeLabel : moreLabel;
            Meterman.ui.showTextDialog(header, b.getPassage(passageNames[i]), label);
        }
    }

    /** Like {@link #showPassages(TextBundle, String, String...)} but using default button labels. */
    public static void showPassages(TextBundle b, String header, String... passageNames) {
        if (defaultMoreLabel == null) {
            defaultMoreLabel = Meterman.systemBundle.getPassage("more-button-label");
            defaultCloseLabel = Meterman.systemBundle.getPassage("close-button-label");
        }
        showPassages(b, header, defaultMoreLabel, defaultCloseLabel, passageNames);
    }

}
