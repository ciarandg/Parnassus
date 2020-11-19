package ui.graphics;

import exceptions.GraphicalCompositionTooLargeException;
import exceptions.GraphicalVoiceOutOfBoundsException;
import model.Composition;
import model.Voice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GraphicalComposition extends JTabbedPane {
    public static final int MAX_TAB_COUNT = 5;
    public static final int NOTES_PER_TAB = 6;
    public static final int MAX_SIZE = MAX_TAB_COUNT * NOTES_PER_TAB;
    private static final int PADDING = 5;

    private Composition composition;

    public GraphicalComposition(Composition composition) throws GraphicalCompositionTooLargeException {
        this.composition = composition;
        int voiceSize = this.composition.getVoice(0).size();

        if (voiceSize > MAX_TAB_COUNT * NOTES_PER_TAB) {
            throw new GraphicalCompositionTooLargeException();
        } else {
            for (int t = 0; t < MAX_TAB_COUNT; t++) {
                int minIndex = t * NOTES_PER_TAB;
                String tabName = (minIndex + 1) + "-" + Integer.min(voiceSize, minIndex + NOTES_PER_TAB);
                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

                for (Voice v : this.composition) {
                    try {
                        panel.add(new PartialGraphicalVoice(v, minIndex, NOTES_PER_TAB));
                    } catch (GraphicalVoiceOutOfBoundsException e) {
                        return;
                    }
                }

                addTab(tabName, panel);
            }
        }
    }
}