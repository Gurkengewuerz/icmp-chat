package de.gurkengewuerz.icmpchat.tray;

import de.gurkengewuerz.icmpchat.ICMPChat;
import org.pmw.tinylog.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ChatTray {

    private TrayIcon icon;

    public ChatTray() {
        if (!SystemTray.isSupported()) return;

        Image image = getImage();
        if (image == null) {
            Logger.error("Couldn't load the image for the tray icon!");
            return;
        }
        icon = new TrayIcon(image, "ICMP Chat");
        icon.addActionListener(e -> {
            JFrame frame = ICMPChat.getFrame();
            frame.toFront();
            frame.repaint();
        });

        SystemTray systemTray = SystemTray.getSystemTray();
        try {
            systemTray.add(icon);
        } catch (AWTException e) {
            Logger.error(e);
            return;
        }
    }

    private Image getImage() {
        BufferedImage trayImage;
        try {
            trayImage = ImageIO.read(getClass().getResource("/images/icon.png"));
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        int trayIconWidth = new TrayIcon(trayImage).getSize().width;
        return trayImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
    }

    public void showMessage(String from, String message) {
        if (icon == null) return;

        icon.displayMessage("New message from " + from, message, TrayIcon.MessageType.INFO);
    }
}
