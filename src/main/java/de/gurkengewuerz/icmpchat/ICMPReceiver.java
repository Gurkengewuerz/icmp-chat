package de.gurkengewuerz.icmpchat;

import de.gurkengewuerz.icmpchat.helper.ICMPChatBox;
import de.gurkengewuerz.icmpchat.object.Device;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV4EchoPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pmw.tinylog.Logger;

import java.io.UnsupportedEncodingException;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPReceiver {

    private ICMPChatBox chatHelper;
    private PcapHandle handle;
    private Device device;
    private boolean running = true;

    public ICMPReceiver(PcapHandle handle, Device device, ICMPChatBox chatHelper) {
        this.chatHelper = chatHelper;
        this.handle = handle;
        this.device = device;
    }

    public void start() {
        try {
            handle.setFilter("icmp and dst host " + device.getOwnIPv4().getHostAddress(), BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            Logger.error(e);
        }
        while (running) {
            Packet p = null;
            try {
                p = handle.getNextPacket();
            } catch (NotOpenException e) {
                Logger.error(e);
            }

            if (p == null) continue;
            if (!p.contains(IcmpV4EchoPacket.class)) continue;
            if (!p.contains(IpV4Packet.class)) continue;
            IcmpV4CommonPacket icmpCommonPacket = p.get(IcmpV4CommonPacket.class);
            IpV4Packet ipV4Packet = p.get(IpV4Packet.class);
            try {
                chatHelper.addReceive(ipV4Packet.getHeader().getSrcAddr().getHostAddress(), new String(icmpCommonPacket.getPayload().getRawData(), "UTF-8").substring(4));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
