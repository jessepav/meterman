package com.illcode.meterman.event;

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
    /** Constant indicating room description text (aka Look text) is ready. */
    public static final int ROOM_DESCRIPTION = 1;

    /** Constant indicating entity description text is ready. */
    public static final int ENTITY_DESCRIPTION = 2;

    /**
     * Called when the text that will be shown for a description has been gathered and is about to be
     * displayed. The listener can affect the text shown by modifying the StringBuilder.
     * @param sb StringBuilder holding the text to be shown
     * @param textType either {@link #ROOM_DESCRIPTION} or {@link #ENTITY_DESCRIPTION}, indicating at
     *      what point the method is being called.
     */
    void descriptionTextReady(StringBuilder sb, int textType);
}
