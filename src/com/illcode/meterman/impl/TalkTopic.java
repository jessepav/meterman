package com.illcode.meterman.impl;

import java.util.List;

/**
 * Represents a topic of discussion with an NPC
 * <p/>
 * NOTE: since <tt>TalkTopic</tt>s are always retrieved from a {@link WorldBuilder#loadTopicMap(String)
 * topicMap}, and thus the same TalkTopic instance will always be retrieved for a given key, we can compare
 * and hash <tt>TalkTopic</tt>s by identity, and don't need to write <tt>hashCode</tt> and <tt>equals</tt>
 * methods.
 */
public class TalkTopic
{
    /** Unique topic key, as found in the topic map */
    public String key;

    /** Topic as shown to the user in the list dialog. There can be multiple <tt>TalkTopic</tt>s
     *  with the same label, if the NPC will say different things depending on circumstances. */
    public String label;

    /** What the NPC will say for this topic. */
    public String text;

    /** If this topic is selected, these topics should be added to the topic list */
    public List<TalkTopic> addTopics;

    /** If this topic is selected, these topics should be removed from the topic list */
    public List<TalkTopic> removeTopics;

    public TalkTopic() {
    }

    // This is used by ui.showListDialog()
    public String toString() {
        return label;
    }
}
