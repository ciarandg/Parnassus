package ui;

import exceptions.*;
import model.Composition;
import persistence.JsonReader;
import persistence.JsonWriter;
import ui.graphics.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GUI extends JFrame {
    private static final int WINDOW_PADDING_X = 10;
    private static final int WINDOW_PADDING_Y = 0;
    private static final int BUTTON_COUNT = 4;
    private static final int BUTTON_VGAP = 10;
    private static final int DEFAULT_COMPOSITION_SIZE = 8;
    private static final String PASS_AUDIO_PATH = "./assets/pass.wav";
    private static final String FAIL_AUDIO_PATH = "./assets/fail.wav";
    private final JFileChooser fc = new JFileChooser();

    private JPanel mainPanel;
    private GraphicalComposition graphicalComposition;
    private ButtonPanel buttonPanel;
    private JsonReader jsr;
    private JsonWriter jsw;
    private Clip passClip;
    private Clip failClip;

    // EFFECTS: construct and make visible a window containing:
    //          a GraphicalComposition, a new composition button, a load button, a save button, and a validate button
    public GUI() {
        super("Parnassus");
        Composition cmp = new Composition(DEFAULT_COMPOSITION_SIZE);

        try {
            graphicalComposition = new GraphicalComposition(cmp);
        } catch (GraphicalCompositionTooLargeException e) {
            e.printStackTrace();
            return;
        }

        initAudio();
        initGraphics();
    }

    // CITATION: https://www.codeproject.com/Answers/1210277/Play-wav-file-in-java#answer1
    // MODIFIES: this
    // EFFECTS: load audio clips for validator results into memory
    private void initAudio() {
        try {
            passClip = AudioSystem.getClip();
            passClip.open(AudioSystem.getAudioInputStream(new File(PASS_AUDIO_PATH)));
            failClip = AudioSystem.getClip();
            failClip.open(AudioSystem.getAudioInputStream(new File(FAIL_AUDIO_PATH)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MODIFIES: this
    // EFFECTS:  draws the JFrame window where this DrawingEditor will operate, and populates the tools to be used
    //           to manipulate this drawing
    private void initGraphics() {
        initMainPanel();

        Dimension minWindowSize = new Dimension(mainPanel.getPreferredSize().width + WINDOW_PADDING_X,
                                                mainPanel.getPreferredSize().height + WINDOW_PADDING_Y);
        setPreferredSize(minWindowSize);
        setMinimumSize(minWindowSize);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initMainPanel() {
        mainPanel = new JPanel();
        buttonPanel = new ButtonPanel();
        mainPanel.add(graphicalComposition);
        mainPanel.add(buttonPanel);
        add(mainPanel);
    }

    private void setComposition(Composition cmp) throws GraphicalCompositionTooLargeException {
        GraphicalComposition newGraphicalComposition = new GraphicalComposition(cmp);
        mainPanel.remove(graphicalComposition);
        graphicalComposition = newGraphicalComposition;
        mainPanel.add(graphicalComposition, 0);
        mainPanel.validate();
    }

    public static void main(String[] args) {
        new GUI();
    }

    private class ButtonPanel extends JPanel {
        private ParnassusButton newCompositionButton;
        private ParnassusButton loadButton;
        private ParnassusButton saveButton;
        private ParnassusButton validationButton;
        private GridLayout layout;

        public ButtonPanel() {
            super(new GridLayout(BUTTON_COUNT, 1));
            layout = (GridLayout) getLayout();
            layout.setVgap(BUTTON_VGAP);

            newCompositionButton = new ParnassusButton("New Composition", new NewCompositionButtonListener());
            loadButton = new ParnassusButton("Load", new LoadButtonListener());
            saveButton = new ParnassusButton("Save", new SaveButtonListener());
            validationButton = new ParnassusButton("Validate", new ValidationButtonListener());

            add(newCompositionButton);
            add(loadButton);
            add(saveButton);
            add(validationButton);
        }
    }

    private class NewCompositionButtonListener extends ParnassusButtonListener {
        private static final int DIALOG_WIDTH = 400;
        private static final int DIALOG_HEIGHT = 200;
        private static final int LAYOUT_ROWS = 3;
        private static final int LAYOUT_COLS = 1;
        private static final int DEFAULT_SIZE = 8;
        private static final int MIN_SIZE = 2;

        private JDialog dialog;
        private GridLayout layout;
        private JLabel label;
        private JSpinner spinner;

        @Override
        public void mouseClicked(MouseEvent e) {
            layout = new GridLayout(LAYOUT_ROWS, LAYOUT_COLS);
            dialog = new JDialog();
            dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setContentPane(new JPanel(layout));
            dialog.setVisible(true);

            label = new JLabel("Notes per voice (max " + GraphicalComposition.MAX_SIZE + "):");
            label.setHorizontalAlignment(JLabel.CENTER);
            spinner = new JSpinner(new SpinnerNumberModel(DEFAULT_SIZE, MIN_SIZE, GraphicalComposition.MAX_SIZE, 1));

            dialog.add(label);
            dialog.add(spinner);
            dialog.add(new ParnassusButton("Confirm", new ConfirmButtonListener()));

        }

        private class ConfirmButtonListener extends ParnassusButtonListener {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    setComposition(new Composition((int) spinner.getValue()));
                    dialog.setVisible(false);
                } catch (GraphicalCompositionTooLargeException graphicalCompositionTooLargeException) {
                    // NOTE: spinner already limits composition's size to GraphicalComposition's maximum
                    graphicalCompositionTooLargeException.printStackTrace();
                }
            }
        }
    }

    private class LoadButtonListener extends ParnassusButtonListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                jsr = new JsonReader(fc.getSelectedFile().getPath());
                try {
                    Composition cmp = jsr.read();
                    setComposition(cmp);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (GraphicalCompositionTooLargeException graphicalCompositionTooLargeException) {
                    JOptionPane.showMessageDialog(null, "Composition Too Large",
                                                    "Error: Composition Exceeds Maximum Size",
                                                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class SaveButtonListener extends ParnassusButtonListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                jsw = new JsonWriter(fc.getSelectedFile().getPath());
                try {
                    jsw.open();
                    jsw.write(GraphicalComposition.getComposition());
                    jsw.close();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        }
    }

    private class ValidationButtonListener extends ParnassusButtonListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            String validationMessage = "Your composition is valid!";
            int messageType = JOptionPane.INFORMATION_MESSAGE;
            boolean valid = false;
            try {
                graphicalComposition.getComposition().validate();
                valid = true;
            } catch (VoiceNotFullException voiceNotFullException) {
                validationMessage = "At least one voice is not full.";
            } catch (NotAllIntervalsConsonantException notAllIntervalsConsonantException) {
                validationMessage = "Not all intervals are consonant.";
            } catch (FirstIntervalNotPerfectException firstIntervalNotPerfectException) {
                validationMessage = "The first interval is not a perfect consonance.";
            } catch (LastIntervalNotPerfectException lastIntervalNotPerfectException) {
                validationMessage = "The last interval is not a perfect consonance.";
            } catch (ParallelToPerfectException parallelToPerfectException) {
                validationMessage = "At least one perfect consonance is approached by parallel motion.";
            }

            if (!valid) {
                validationMessage = "Invalid! " + validationMessage;
                messageType = JOptionPane.ERROR_MESSAGE;
            }

            playClip(valid ? passClip : failClip);
            JOptionPane.showMessageDialog(null, validationMessage, "Validation Results", messageType);
        }

        public void playClip(Clip clip) {
            clip.setFramePosition(0);
            clip.start();
        }
    }
}
