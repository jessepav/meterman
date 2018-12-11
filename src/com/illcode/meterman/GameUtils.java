package com.illcode.meterman;

import com.illcode.meterman.ui.MetermanUI;
import org.apache.commons.lang3.text.WordUtils;

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
            TextBundle sysBundle = Meterman.getSystemBundle();
            defaultMoreLabel = sysBundle.getPassage("more-button-label");
            defaultCloseLabel = sysBundle.getPassage("close-button-label");
        }
        showPassages(b, header, defaultMoreLabel, defaultCloseLabel, passageNames);
    }

    /**
     * Ensures that somewhere up the parent chain, this bundle has a certain parent bundle.
     * @param b text bundle
     * @param parentToEnsure the parent bundle we want to ensure is in the chain
     */
    public static void ensureBundleHasParent(TextBundle b, TextBundle parentToEnsure) {
        if (b == null || b == parentToEnsure)
            return;
        TextBundle parent = b.getParent();
        while (parent != null) {
            if (parent == parentToEnsure)
                return;
            b = parent;
            parent = b.getParent();
        }
        b.setParent(parentToEnsure);
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the"), taking into
     * account proper names.
     * @param e entity
     * @param capitalize whether to capitalize the definite article
     */
    public static String defName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name == null || name.isEmpty())
            return "";
        if (e.checkAttribute(Attributes.PROPER_NAME))
            return name;
        String defArt = capitalize ? "The " : "the ";
        return defArt + name;
    }

    /**
     * Return the name of the entity prefixed by the definite article ("the") in lowercase.
     * @param e entity
     */
    public static String defName(Entity e) {
        return defName(e, false);
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other"), taking into account
     * proper names. If {@code e.getIndefiniteArticle()} returns null, we use "an" for names that begin with
     * a vowel, and "a" otherwise.
     * @param e entity
     * @param capitalize whether to capitalize the indefinite article
     */
    public static String indefName(Entity e, boolean capitalize) {
        String name = e.getName();
        if (name == null || name.isEmpty())
            return "";
        if (e.checkAttribute(Attributes.PROPER_NAME))
            return name;
        String indefArt = e.getIndefiniteArticle();
        if (indefArt == null) {
            char c = Character.toLowerCase(name.charAt(0));
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
                indefArt = "an";
            else
                indefArt = "a";
        }
        if (capitalize)
            indefArt = WordUtils.capitalize(indefArt);
        return indefArt + " " + name;
    }

    /**
     * Return the name of the entity prefixed by the indefinite article ("a/an/other") in lowercase.
     * @param e entity
     */
    public static String indefName(Entity e) {
        return indefName(e, false);
    }
}
