package com.illcode.meterman;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.illcode.meterman.ui.MetermanUI;
import static com.illcode.meterman.Attributes.*;
import static com.illcode.meterman.Utils.logger;

import com.illcode.meterman.ui.SoundManager;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


/**
 * Utility methods that apply to the game world or interface.
 */
public final class GameUtils
{
    private static String defaultMoreLabel;
    private static String defaultCloseLabel;

    /**
     * Returns the subset of a given list of entities that have a specific attribute value.
     * @param entities list of entities to filter
     * @param attribute attribute to filter by
     * @param value the value of the attribute
     * @return a new list of entities that have {@code attribute = value}
     */
    public static List<Entity> filterByAttribute(List<Entity> entities, int attribute, boolean value) {
        List<Entity> filteredList = new LinkedList<>();
        filterByAttribute(entities, attribute, value, filteredList);
        return filteredList;
    }

    /**
     * Adds the subset of a given list of entities that have a specific attribute value to a target list.
     * @param entities list of entities to filter
     * @param attribute attribute to filter by
     * @param value the value of the attribute
     * @param target the list to which filtered entities will be added
     */
    public static void filterByAttribute(List<Entity> entities, int attribute, boolean value, List<Entity> target) {
        for (Entity e : entities)
            if (e.checkAttribute(attribute) == value)
                target.add(e);
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

    /** Like {@link #showPassages} but using default button labels.
     * We called the method <tt>showPassagesD</tt> because the varargs makes it tricky to disambiguate
     * overloaded methods with the same parameter types. */
    public static void showPassagesD(TextBundle b, String header, String... passageNames) {
        if (defaultMoreLabel == null) {
            TextBundle sysBundle = Meterman.getSystemBundle();
            defaultMoreLabel = sysBundle.getPassage("more-button-label");
            defaultCloseLabel = sysBundle.getPassage("close-button-label");
        }
        showPassages(b, header, defaultMoreLabel, defaultCloseLabel, passageNames);
    }

    /**
     * Shows a sequence of "F" passages, that is, passages that specify their own header and button
     * labels. Here is an example F-passage:
     * <pre>{@code
     * [ f-passage ]
     *
     *          This is the header line
     * And then the regular bunch of text that is
     * going to be shown in the TextDialog.
     *      Button Label
     * }</pre>
     * Note that the indentation doesn't matter, and is there only for visual clarity.
     * <ul>
     *     <li>Header label: first line of the passage, whitespace trimmed.</li>
     *     <li>Passage text: from the second through the second-to-last line.</li>
     *     <li>Button label: last line of the passage, whitespace trimmed.</li>
     * </ul>
     * @param b text bundle
     * @param passageNames bundle passages to show
     */
    public static void showPassagesF(TextBundle b, String... passageNames) {
        for (String name : passageNames) {
            String text = b.getPassage(name);
            int firstNewline = text.indexOf('\n');
            int lastNewline = text.lastIndexOf('\n');
            if (firstNewline == -1 || firstNewline == lastNewline)
                continue;  // an invalid F-passage
            String header = text.substring(0, firstNewline).trim();
            String buttonLabel = text.substring(lastNewline+1).trim();
            String passageText = text.substring(firstNewline + 1, lastNewline);
            Meterman.ui.showTextDialog(header, passageText, buttonLabel);
        }
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
        if (e.checkAttribute(PROPER_NAME))
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
        if (e.checkAttribute(PROPER_NAME))
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

    /**
     * Returns a list of all the {@link Attributes#TAKEABLE} entities in the current room and the player
     * inventory. This is useful in situations, for instance, where some object has a slot that the
     * player can put something into. The object has an action "Put in Slot" that, when activated, will
     * prompt the player to choose what to put it -- and presumably only things that are <tt>TAKEABLE</tt> can
     * be lifted and put in.
     */
    public static List<Entity> getCurrentTakeableEntities() {
        List<Entity> takeables = new LinkedList<>();
        getCurrentTakeableEntities(takeables);
        return takeables;
    }

    /**
     * Like {@link #getCurrentTakeableEntities()}, but uses a list given as a parameter to avoid allocation.
     */
    public static void getCurrentTakeableEntities(List<Entity> takeables) {
        filterByAttribute(Meterman.gm.getCurrentRoom().getRoomEntities(), TAKEABLE, true, takeables);
        filterByAttribute(Meterman.gm.getPlayer().inventory, TAKEABLE, true, takeables);
    }

    /**
     * Reads a JSON resource definition in this format:
     * <pre>{@code
     * {
     *     "sounds" : {
     *         "soundName1" : "sound-asset-path1",
     *         "soundName2" : "sound-asset-path2",
     *         ...
     *     },
     *     "music" : {
     *         "musicName1" : "music-asset-path1",
     *         "musicName2" : "music-asset-path2",
     *         ...
     *     },
     *     "images" : {
     *         "imageName1" : "image-asset-path1",
     *         "imageName2" : "image-asset-path2",
     *         ...
     *     }
     * }
     * }</pre>
     * and loads the listed sounds and images via {@link SoundManager#loadSound(String, Path)},
     * {@link SoundManager#loadMusic(String, Path)}, and {@link MetermanUI#loadImage(String, Path)},
     * respectively.
     * <p/>
     * Normally, you would call this in {@link Game#init()}, if the resources are global, or {@link Game#start(boolean)},
     * if the resources are specific to a particular map section or segment of the game.
     * @param json JSON resource definition
     */
    public static void loadResources(String json) {
        try {
            JsonObject o = Json.parse(json).asObject();
            JsonValue v = o.get("sounds");
            if (v != null) {
                for (JsonObject.Member m : v.asObject())
                    Meterman.sound.loadSound(m.getName(), Utils.pathForGameAsset(m.getValue().asString()));
            }
            v = o.get("music");
            if (v != null) {
                for (JsonObject.Member m : v.asObject())
                    Meterman.sound.loadMusic(m.getName(), Utils.pathForGameAsset(m.getValue().asString()));
            }
            v = o.get("images");
            if (v != null) {
                for (JsonObject.Member m : v.asObject())
                    Meterman.ui.loadImage(m.getName(), Utils.pathForGameAsset(m.getValue().asString()));
            }
        } catch (ParseException | UnsupportedOperationException ex) {
            logger.log(Level.WARNING, "JSON error, GameUtils.loadResources()", ex);
        }
    }
}
