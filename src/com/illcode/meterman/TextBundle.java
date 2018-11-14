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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.illcode.meterman.Utils.logger;

/**
 * A TextBundle is a container for named text passages, also supporting variable subtitution.
 * <p/>
 * Here is an example bundle illustrating the file format:
 * <pre>{@code
 [intro]

 This is a "text bundle" (handled by the TextBundle class). It's an easy
 way to store and reference passages of text.

 Each passage is headed by a "passage name" between square brackets
 ("intro", in this case) and comprises the first non-blank line to the
 last non-blank line, inclusive.


 [passage-2]

 The passages can be retrieved complete with line breaks and whitespace
 preserved

 +-------------------------------------------+
 |                                           |
 |         Like this beautiful box           |
 |                                           |
 +-------------------------------------------+

 Or with all whitespace and newlines collapsed.


 [short]

 A one-line passage.

 * }</pre>
 */
public final class TextBundle
{
    /** We cache the {@code '\s+'} Pattern to avoid recompilation.
     *  @see #getPassageWrapped(String, int) */
    private static Pattern whiteSpacePattern;

    /** @see #getPassageFlowed(String)  */
    private static Pattern flowPattern;

    private Map<String, String> passageMap;

    private StrSubstitutor sub;
    private Map<String,String> subMap;

    /** Construct an empty TextBundle */
    public TextBundle() {
        passageMap = new HashMap<>();
        subMap = new HashMap<>();
        sub = new StrSubstitutor(subMap);
        sub.setValueDelimiter('|');
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

    /**
     * Returns a passage with newlines intact.
     * @param name passage heading name
     * @return passage text or "" if the passage doesn't exist
     */
    public String getPassage(String name) {
        if (!passageMap.containsKey(name))
            return "";

        return sub.replace(passageMap.get(name));
    }

    /**
     * Returns a passage wrapped to a specified column.
     * @param name passage heading name
     * @param col column to wrap to. 0 indicates text will be returned all on one line.
     *            -1 indicates text should not be wrapped (just like {@link #getPassage(String)})
     * @return wrapped passage text or "" if the passage doesn't exist
     */
    public String getPassageWrapped(String name, int col) {
        if (!passageMap.containsKey(name))
            return "";
        if (col == -1)
            return getPassage(name);

        if (whiteSpacePattern == null)
            whiteSpacePattern = Pattern.compile("\\s+");
        String text = whiteSpacePattern.matcher(passageMap.get(name)).replaceAll(" ");
        if (col > 0)
            text = WordUtils.wrap(text, col);
        return sub.replace(text);
    }

    /**
     * Returns a passage with each paragraph flowed into one line, but paragraph
     * breaks (two or more newlines in sequence) left intact.
     * @param name passage heading name
     * @return flowed passage text
     */
    public String getPassageFlowed(String name) {
        if (!passageMap.containsKey(name))
            return "";
        if (flowPattern == null)
            flowPattern = Pattern.compile("(?<!\\n)\\n(?!\\n)");
        return flowPattern.matcher(passageMap.get(name)).replaceAll(" ");
    }

    /**
     * Returns a set of the names of the passages contained in this text bundle.
     */
    public Set<String> getPassageNames() {
        return passageMap.keySet();
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
     * Load a TextBundle from a given Path. The format of text bundles is given in
     * the {@link TextBundle class notes}.
     * @param p path of bundle file
     * @return a new TextBundle containing passages from the file
     */
    public static TextBundle loadBundle(Path p) {
        ArrayList<String> passageLines = new ArrayList<>(100);
        String name = null;  // name of the current passage

        TextBundle b = new TextBundle();
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String newName = getPassageName(line);
                if (newName != null) {   // we've encountered a new heading
                    if (name != null) {  // we need to save the current passage
                        b.passageMap.put(name, gatherPassage(passageLines));
                        passageLines.clear();
                    }
                    name = newName;
                } else if (name != null) {   // we're reading passage lines
                    passageLines.add(line);
                }
                // else we're not reading passage lines at all, so forget about it
            }
            if (name != null)   // save the last passage, if any
                b.passageMap.put(name, gatherPassage(passageLines));
        } catch (IOException e) {
            logger.log(Level.WARNING, "loadBundle()", e);
        }
        return b;
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
     */
    private static String getPassageName(String line) {
        int len = line.length();
        if (len >= 3 && line.charAt(0) == '[' && line.charAt(len - 1) == ']') {
            return line.substring(1, len - 1);
        }
        return null;
    }
}
