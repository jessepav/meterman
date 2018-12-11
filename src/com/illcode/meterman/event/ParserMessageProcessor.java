package com.illcode.meterman.event;

import com.illcode.meterman.Entity;

/**
 * Normally the GameManager prints a parser-like message when an action is performed, like {@code "> TAKE
 * CLOAK"}. A ParserMessageProcessor can request that this parser message replaced with its own message, or
 * suppressed entirely.
 */
public interface ParserMessageProcessor
{
    /**
     * Called when an action is performed.
     * @param e selected entity performing the action
     * @param action the action that is being performed
     * @return null to allow the normal parser message flow to continue, <tt>""</tt> to suppress the
     *         parser message entirely, or a non-empty string to replace the default parser message.
     */
    String replaceParserMessage(Entity e, String action);
}
