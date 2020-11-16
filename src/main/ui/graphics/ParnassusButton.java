package ui.graphics;

import javax.swing.*;

public class ParnassusButton extends JButton {
    public ParnassusButton(String buttonText, ParnassusButtonListener listener) {
        super(buttonText);
        addMouseListener(listener);
    }
}