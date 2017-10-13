package de.gurkengewuerz.icmpchat.helper;

import javax.swing.*;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPChatBox {
    private JTextPane chatBox;
    private JScrollPane scrollPane;

    public ICMPChatBox(JTextPane chatBox, JScrollPane scrollPane) {
        this.chatBox = chatBox;
        this.scrollPane = scrollPane;
    }

    public void addMessage(String myMessage) {
        addLine("-> " + myMessage);
    }

    public void addReceive(String from, String fromMessage) {
        addLine(from + ": " + fromMessage);
    }

    public void addLine(String text) {
        chatBox.setText(chatBox.getText() + "\n" + text);
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }
}
