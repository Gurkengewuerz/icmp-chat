package de.gurkengewuerz.icmpchat;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.gurkengewuerz.icmpchat.helper.ICMPChatBox;
import de.gurkengewuerz.icmpchat.helper.ICMPSession;
import de.gurkengewuerz.icmpchat.object.Device;
import de.gurkengewuerz.icmpchat.object.ICMPInterface;
import de.gurkengewuerz.icmpchat.tray.ChatTray;
import org.apache.commons.io.IOUtils;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPChatterGUI {
    private static final int MAX_CHARS = 140;
    private JTabbedPane tabbedPane1;
    public JPanel mainPanel;
    private JComboBox targetNetwork;
    private JTextArea targetList;
    private JTextPane chatBox;
    private JFormattedTextField chatField;
    private JButton chatSend;
    private JScrollPane scrollPane;
    private JLabel countleft;

    private ChatTray tray;
    private ICMPChatBox chatHelper;
    private PcapNetworkInterface device;
    private PcapHandle readHandle;
    private PcapHandle sendHandle;
    private PcapHandle contunuisReader;
    private ICMPSession session;
    private ICMPReceiver receiver;
    private Thread receiverThread;

    public ICMPChatterGUI() {
        chatHelper = new ICMPChatBox(chatBox, scrollPane);
        tray = new ChatTray();

        try {
            List<PcapNetworkInterface> deviceList = Pcaps.findAllDevs();
            targetNetwork.addItem(new ICMPInterface("", null));
            for (PcapNetworkInterface networkInterface : deviceList) {
                targetNetwork.addItem(new ICMPInterface(networkInterface.getDescription(), networkInterface));
            }
        } catch (PcapNativeException e) {
            Logger.error(e);
        }

        chatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                chatField.setText(chatField.getText().substring(0, Math.min(chatField.getText().length(), MAX_CHARS)));

                int i = chatField.getText().length();
                countleft.setText(i + "/" + MAX_CHARS);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    chatSend.doClick();
                }
            }
        });

        targetNetwork.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ICMPInterface item = (ICMPInterface) e.getItem();
                if (item.getValue() == null) return;
                //initialize Jpcap
                try {
                    if (receiver != null) {
                        receiver.stop();
                        receiverThread.interrupt();
                    }
                    if (readHandle != null) readHandle.close();
                    if (sendHandle != null) sendHandle.close();
                    if (contunuisReader != null) contunuisReader.close();
                    device = item.getValue();
                    Device deviceData = new Device(device);
                    readHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 100);
                    sendHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 100);
                    contunuisReader = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 100);
                    session = new ICMPSession(sendHandle, readHandle, deviceData);
                    receiver = new ICMPReceiver(contunuisReader, deviceData, chatHelper, tray);
                    receiverThread = new Thread(() -> receiver.start());
                    receiverThread.start();
                } catch (Exception ex) {
                    Logger.error(ex);
                }
            }
        });

        chatSend.addActionListener(e -> {
            if (session == null || session.getHandle() == null || session.getDevice() == null) return;
            if (chatField.getText().isEmpty() || !(chatField.getText().trim().length() > 0)) return;
            if (targetList.getText().isEmpty() || !(targetList.getText().trim().length() > 0)) return;
            countleft.setText("0/" + MAX_CHARS);
            String text = chatField.getText();
            byte[] utf8Bytes = new byte[0];
            try {
                utf8Bytes = text.substring(0, Math.min(text.length(), MAX_CHARS)).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                Logger.error(e1);
            }
            if (utf8Bytes.length > 65500) {
                JOptionPane.showMessageDialog(null,
                        "Text is to large for ICMP.",
                        "ICMP LENGTH ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            chatHelper.addMessage(text);
            chatField.setText("");

            String targetdevices = targetList.getText();
            List<String> lines = new ArrayList<>();
            try {
                lines = IOUtils.readLines(new StringReader(targetdevices));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            for (String line : lines) {
                if (!isIPvAddress(line)) return;
                try {
                    session.sendICMP(Inet4Address.getByName(line), utf8Bytes);
                } catch (UnknownHostException e1) {
                    Logger.error(e1);
                }
            }
        });
    }

    public static Boolean isIPvAddress(String address) {
        if (address.isEmpty()) {
            return false;
        }
        try {
            Object res = InetAddress.getByName(address);
            return res instanceof Inet4Address || res instanceof Inet6Address;
        } catch (final UnknownHostException ex) {
            return false;
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1 = new JTabbedPane();
        mainPanel.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Settings", panel1);
        final JLabel label1 = new JLabel();
        label1.setText("Network Devices");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetNetwork = new JComboBox();
        panel1.add(targetNetwork, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Target Devices");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetList = new JTextArea();
        panel1.add(targetList, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Chat", panel2);
        scrollPane = new JScrollPane();
        panel2.add(scrollPane, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatBox = new JTextPane();
        chatBox.setEditable(false);
        scrollPane.setViewportView(chatBox);
        chatField = new JFormattedTextField();
        panel2.add(chatField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        chatSend = new JButton();
        chatSend.setText("Send");
        panel2.add(chatSend, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        countleft = new JLabel();
        countleft.setText("0/140");
        panel2.add(countleft, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
