package com.illcode.meterman.event;

import com.illcode.meterman.Entity;
import com.illcode.meterman.Room;

/**
 * A DescriptionTextProcessor is notified when the text that will be displayed for a Room or Entity
 * description (via a Look command or entity selection) has been gathered and is ready for display in the UI.
 * <p/>
 * This mechanism can be used, for example, to humorous effect ("Through your mighty -8.00 diopter glasses
 * you see..."), or to simulate status effects (ex. blanking out every 3rd character to simulate visual
 * distress).
 */
public interface DescriptionTextProcessor
{
    /**
     * Called when the text that will be shown for a room description has been gathered and is about to be
     * displayed. The listener can affect the text shown by modifying the StringBuilder.
     * @param sb StringBuilder holding the text to be shown
     * @param r room whose description is being displayed
     */
    void roomDescriptionTextReady(StringBuilder sb, Room r);

    /**
     * Called when the text that will be shown for an entity description has been gathered and is about to be
     * displayed. The listener can affect the text shown by modifying the StringBuilder.
     * @param sb StringBuilder holding the text to be shown
     * @param e entity whose description is being displayed
     */
    void entityDescriptionTextReady(StringBuilder sb, Entity e);
}
