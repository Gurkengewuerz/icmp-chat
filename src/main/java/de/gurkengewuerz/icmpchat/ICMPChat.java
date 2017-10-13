package de.gurkengewuerz.icmpchat;

import javax.swing.*;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPChat {
    public static void main(String... args) {
        JFrame frame = new JFrame("ICMPChatterGUI");
        frame.setContentPane(new ICMPChatterGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
