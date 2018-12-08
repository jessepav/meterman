package com.illcode.meterman;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.text.WordUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.illcode.meterman.Utils.logger;

/**
 * A TextBundle is a container for named text passages, also supporting variable subtitution, and parent
 * bundle chaining.
 * <p/>
 * Here is an example bundle illustrating the file format:
 * <pre>{@code
[passage-name]

Each passage is headed by a "passage name" between square brackets
("intro", in this case) and comprises the first non-blank line to the
last non-blank line, inclusive.

The passages can be retrieved complete with line breaks and whitespace
preserved, with all whitespace and newlines collapsed, word-wrapped to
a specified column, or "flowed" so that paragraph breaks are preserved
by newlines within a paragraph are replaced by spaces.

[#Information Passage]

Passages that start with the pound character "#" will not be saved into
the bundle, but are simply commentary passages in the text.

[Multiline Passage (flowed)]

If a passage name ends with the string "(flowed)" its text with be flowed
automatically, as though calls to getPassage() were calls to getPassageFlowed().
Note that the name of the passage will be the original passage name without
"(flowed)" and with whitespace trimmed off of both ends. Thus this passage
would have a name of "Multiline Passage".
 * }</pre>
 *
 * <em>NOTE</em>: <tt>TextBundle</tt> instances should not be stored as fields in any
 * class that will become part of the game's {@link WorldState}, because it won't
 * persist properly. So how should game classes use bundles? The Game's {@link Game#init()}
 * method should load the bundle and put it somewhere its objects can find it:
 * <ul>
 *     <li>As the game bundle, via {@link Meterman#setGameBundle(TextBundle)}</li>
 *     <li>In a static field of the Game implementation class itself.</li>
 * </ul>
 */
public final class TextBundle
{
    private static final String FLOWED_SUFFIX = "(flowed)";

    /** We cache the {@code '\s+'} Pattern to avoid recompilation.
     *  @see #getPassageWrapped(String, int) */
    private static Pattern whiteSpacePattern;

    /** @see #getPassageFlowed(String)  */
    private static Pattern flowPattern;

    private Map<String, String> passageMap;

    private StrSubstitutor sub;
    private Map<String,String> subMap;

    private TextBundle parent;

    public TextBundle() {
        this(null);
    }

    /**
     * Construct an empty TextBundle.
     * @param parent parent bundle
     */
    public TextBundle(TextBundle parent) {
        this.parent = parent;
        passageMap = new HashMap<>();
        subMap = new HashMap<>();
        sub = new StrSubstitutor(subMap);
        sub.setValueDelimiter('|');
    }

    public TextBundle getParent() {
        return parent;
    }

    public void setParent(TextBundle parent) {
        this.parent = parent;
    }

    /**
     * Text bundle supports variable substition, using
     * <a href="https://jessepav.github.io/java-api-docs/commons-lang3-3.4/apidocs/org/apache/commons/lang3/text/StrSubstitutor.html">
     * Apache StrSubstitutor</a> with a default value delimeter of {@code '|'}
     * @param varname variable name
     * @param val the value of the variable when performing substitions
     */
    public void addSubstitution(String varname, String val) {
        subMap.put(varname, val);
    }

    /**
     * Adds all substitutions from a given Map
     * @param subs map from which to take substitutions
     */
    public void addSubstitutions(Map<String,String> subs) {
        subMap.putAll(subs);
    }

    /**
     * Sets the internal substitution map to those in a given Map
     * @param subs map whose entries will replace our internal substituions
     */
    public void setSubstitutions(Map<String,String> subs) {
        subMap.clear();
        subMap.putAll(subs);
    }

    /**
     * Remove a variable from our map of substitutions.
     * @param varname variable name
     */
    public void removeSubstitution(String varname) {
        subMap.remove(varname);
    }

    /** Clear all variable substitutions. */
    public void clearSubstitutions() {
        subMap.clear();
    }

    /** Returns true if this bundle, or any bundle up its parent chain, contains a passage
     *  with the given name. */
    public boolean hasPassage(String name) {
        if (passageMap.containsKey(name))
            return true;
        else if (parent != null)
            return parent.hasPassage(name);
        else
            return false;
    }

    /**
     * Returns a passage with newlines intact.
     * @param name passage heading name
     * @return passage text or "" if the passage doesn't exist
     */
    public String getPassage(String name) {
        String s = passageMap.get(name);
        if (s != null)
            return sub.replace(s);
        else if (parent != null)
            return parent.getPassage(name);
        else
            return "";
    }

    /**
     * Returns a passage wrapped to a specified column.
     * @param name passage heading name
     * @param col column to wrap to. 0 indicates text will be returned all on one line.
     *            -1 indicates text should not be wrapped (just like {@link #getPassage(String)})
     * @return wrapped passage text or "" if the passage doesn't exist
     */
    public String getPassageWrapped(String name, int col) {
        if (col == -1)
            return getPassage(name);
        String s = getPassage(name);
        if (s.isEmpty())
            return "";

        if (whiteSpacePattern == null)
            whiteSpacePattern = Pattern.compile("\\s+");
        String text = whiteSpacePattern.matcher(s).replaceAll(" ");
        if (col > 0)
            text = WordUtils.wrap(text, col);
        return sub.replace(text);
    }

    /**
     * Returns a passage with each paragraph flowed into one line, but paragraph
     * breaks (two or more newlines in sequence) left intact.
     * @param name passage heading name
     * @return flowed passage text, or "" if the named passage doesn't exit
     */
    public String getPassageFlowed(String name) {
        return flowText(getPassage(name));
    }

    private static String flowText(String text) {
        if (text.isEmpty())
            return "";
        if (flowPattern == null)
            flowPattern = Pattern.compile("(?<!\\n)\\n(?!\\n)");
        return flowPattern.matcher(text).replaceAll(" ");
    }

    /**
     * Returns a set of the names of the passages contained in this text bundle.
     */
    public Set<String> getPassageNames() {
        return passageMap.keySet();
    }

    /**
     * Returns a set of the names of the passages contained in this text bundle
     * and all of its parents.
     */
    public Set<String> getAllPassageNames() {
        Set<String> names = new HashSet<>(200);
        for (TextBundle b = this; b != null; b = b.parent)
            names.addAll(b.passageMap.keySet());
        return names;
    }

    /**
     * Sets the text of a named passage in this bundle.
     * @param name passage name
     * @param text text of the passage
     */
    public void putPassage(String name, String text) {
        passageMap.put(name, text);
    }
    
    /**
     * Put all of the passages from a given Map into this text bundle.
     * @param passages passages to add
     */
    public void putPassages(Map<String,String> passages) {
        passageMap.putAll(passages);
    }
    
    /**
     * Remove all passages from this text bundle.
     */
    public void clearPassages() {
        passageMap.clear();
    }

    /**
     * Load a TextBundle from a given Path. The format of text bundles is given in
     * the {@link TextBundle class notes}.
     * @param p path of bundle file
     * @return a new TextBundle containing passages from the file, with no parent
     */
    public static TextBundle loadBundle(Path p) {
        return loadBundle(p, null);
    }

    /**
     * Load a TextBundle from a given Path. The format of text bundles is given in
     * the {@link TextBundle class notes}.
     * @param p path of bundle file
     * @param parent the parent bundle of the loaded TextBundle
     * @return a new TextBundle containing passages from the file
     */
    public static TextBundle loadBundle(Path p, TextBundle parent) {
        ArrayList<String> passageLines = new ArrayList<>(100);
        String name = null;  // name of the current passage

        TextBundle b = new TextBundle(parent);
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String newName = getPassageName(line);
                if (newName != null && !newName.startsWith("#")) {   // we've encountered a new, non-comment heading
                    if (name != null) {  // we need to save the current passage
                        savePassage(b.passageMap, name, gatherPassage(passageLines));
                        passageLines.clear();
                    }
                    name = newName;
                } else if (name != null) {   // we're reading passage lines
                    passageLines.add(line);
                }
                // else we're not reading passage lines at all, so forget about it
            }
            if (name != null)   // save the last passage, if any
                savePassage(b.passageMap, name, gatherPassage(passageLines));
        } catch (IOException e) {
            logger.log(Level.WARNING, "loadBundle()", e);
        }
        return b;
    }

    // This exists to handle special passage name suffixes, like "(flowed)"
    private static void savePassage(Map<String,String> passageMap, String name, String text) {
        if (name.endsWith(FLOWED_SUFFIX)) {
            name = name.substring(0, name.length() - FLOWED_SUFFIX.length()).trim();
            if (!name.isEmpty())
                passageMap.put(name, flowText(text));
        } else {
            passageMap.put(name, text);
        }
    }

    /**
     * Returns the text from the first non-blank line through the last non-blank
     * line of {@code passageLines}, with a newline inserted between each String.
     */
    private static String gatherPassage(ArrayList<String> passageLines) {
        int n = passageLines.size();
        if (n == 0)
            return "";

        int firstNonblankLine, lastNonblankLine;

        for (firstNonblankLine = 0; firstNonblankLine < n; firstNonblankLine++) {
            if (!StringUtils.isWhitespace(passageLines.get(firstNonblankLine)))
                break;
        }
        if (firstNonblankLine == n)
            return "";   // this is an empty passage

        for (lastNonblankLine = n - 1; lastNonblankLine > firstNonblankLine; lastNonblankLine--) {
            if (!StringUtils.isWhitespace(passageLines.get(lastNonblankLine)))
                break;
        }
        if (lastNonblankLine == firstNonblankLine)
            return passageLines.get(firstNonblankLine);  // a one-line passage

        StringBuilder b = new StringBuilder((lastNonblankLine - firstNonblankLine + 1) * 80);
        for (int i = firstNonblankLine; i <= lastNonblankLine; i++) {
            if (i != firstNonblankLine)
                b.append('\n');
            b.append(passageLines.get(i));
        }
        return b.toString();
    }

    /**
     * Returns the passage name indicated by {@code line}, if {@code line} is a
     * passage heading string (i.e. {@code "[<name>]"}), or {@code null} otherwise.
     * @param line text to examine as a potential passage heading
     * @return passage name, with whitespace trimmed off the ends, or null if the name is invalid
     */
    private static String getPassageName(String line) {
        int len = line.length();
        if (len >= 3 && line.charAt(0) == '[' && line.charAt(len - 1) == ']') {
            String name = line.substring(1, len - 1).trim();
            if (!name.isEmpty())
                return name;
        }
        return null;
    }
}
