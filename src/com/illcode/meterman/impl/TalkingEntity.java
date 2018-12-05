package com.illcode.meterman.impl;

import com.illcode.meterman.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.illcode.meterman.Meterman.ui;

/**
 * An entity that supports "conversations" in the form of {@link TalkTopic}S.
 */
public class TalkingEntity extends BaseEntity
{
    /** The text we should show above the topic list in the UI list-dialog. */
    public String dialogText;

    /** Text that should be shown if there are no topics to talk about. */
    public String noTopicsText;

    // TalkingEntity doesn't use the topicMap itself, but it may be useful to subclasses
    // that want to do more advanced conversation systems.
    public Map<String,TalkTopic> topicMap;

    // The topics we'll show in the list when we're Talk'ed to.
    public List<TalkTopic> currentTopics;

    private List<String> actions;

    public TalkingEntity() {
    }

    public void init() {
        super.init();
        actions = new ArrayList<>(6);
        actions.add(BasicActions.getTalkAction());
        topicMap = Collections.emptyMap();
        currentTopics = new ArrayList<>();
        dialogText = "(dialog text)";
    }

    public List<String> getActions() {
        if (delegate != null)
            return delegate.getActions(this);
        else
            return actions;
    }

    public boolean processAction(String action) {
        if (delegate != null)
            return delegate.processAction(this, action);

        if (action.equals(BasicActions.getTalkAction())) {
            processTalkAction();
            return true;
        } else {
            return false;
        }
    }

    /** Processes the Talk action. Kept in a separate method so listeners
        and delegates can call it directly.*/
    public void processTalkAction() {
        if (currentTopics.isEmpty()) {
            ui.appendNewline();
            ui.appendTextLn(noTopicsText);
        } else {
            TalkTopic tt = ui.showListDialog(getName(), dialogText, currentTopics, true);
            if (tt != null) {
                ui.appendNewline();
                ui.appendTextLn(tt.text);
                for (TalkTopic topic : tt.addTopics)
                    if (!currentTopics.contains(topic))
                        currentTopics.add(topic);
                for (TalkTopic topic : tt.removeTopics)
                    currentTopics.remove(topic);
            }
        }
    }
}
