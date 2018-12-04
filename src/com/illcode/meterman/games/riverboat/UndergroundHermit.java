package com.illcode.meterman.games.riverboat;

import com.illcode.meterman.Meterman;
import com.illcode.meterman.Utils;
import com.illcode.meterman.impl.BaseEntity;
import com.illcode.meterman.impl.TalkTopic;

import static com.illcode.meterman.Meterman.ui;

import java.util.*;

public class UndergroundHermit extends BaseEntity
{
    private static final String TALK_ACTION = Utils.getActionName("Talk");

    private Map<String,TalkTopic> topicMap;

    private List<String> actions;
    private List<TalkTopic> currentTopics;
    private boolean monsterTopicAdded;  // an optimization to avoid calling currentTopics.contains(monsterTopic)

    public UndergroundHermit() {
    }

    public void init() {
        super.init();
        actions = new ArrayList<>(4);
        actions.add(TALK_ACTION);
        currentTopics = new ArrayList<>();
        topicMap = Collections.emptyMap();
    }

    public void setTopicMap(Map<String,TalkTopic> topicMap) {
        this.topicMap = topicMap;
        currentTopics.clear();
        currentTopics.add(topicMap.get("hello"));
    }

    public List<String> getActions() {
        return actions;
    }

    public boolean processAction(String action) {
        RiverboatStatePart1 state = RiverboatStatePart1.getRiverboatStatePart1();
        if (action.equals(TALK_ACTION)) {
            if (!monsterTopicAdded && state.monsterSeen) {
                currentTopics.add(topicMap.get("monster"));
                monsterTopicAdded = true;
            }
            TalkTopic tt = ui.showListDialog(getName(), "What shall we talk about?", currentTopics, true);
            if (tt != null) {
                ui.appendNewline();
                ui.appendText(tt.text);
                // Here we can add or remove topics depending on which topic was selected
            }
            return true;
        } else {
            return false;
        }
    }
}
