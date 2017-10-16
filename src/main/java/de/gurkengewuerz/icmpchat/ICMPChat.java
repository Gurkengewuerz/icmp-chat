package de.gurkengewuerz.icmpchat;

import javax.swing.*;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPChat {

    private static JFrame frame;

    public static void main(String... args) {
        frame = new JFrame("ICMPChatterGUI");
        frame.setContentPane(new ICMPChatterGUI().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static JFrame getFrame() {
        return frame;
    }

}
