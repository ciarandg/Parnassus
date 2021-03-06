package ui.graphics;

import exceptions.GraphicalVoiceOutOfBoundsException;
import model.Voice;

import javax.swing.*;
import java.awt.*;

import static java.lang.Integer.min;

// A graphical representation of a sequence of Notes (part of a Voice), comprised of several GraphicalNotes
public class PartialGraphicalVoice extends JPanel {
    private Voice voice;

    // EFFECTS: construct a JPanel containing several instances of GraphicalNote
    //          represents part of a Voice graphically
    public PartialGraphicalVoice(Voice voice, int minIndex, int size) throws GraphicalVoiceOutOfBoundsException {
        super(new GridLayout(1, size));
        this.voice = voice;

        if (minIndex < this.voice.size()) {
            for (int i = 0; i < min(size, voice.size() - minIndex); i++) {
                add(new GraphicalNote(this.voice.getNote(minIndex + i)));
            }
        } else {
            throw new GraphicalVoiceOutOfBoundsException();
        }
    }
}
