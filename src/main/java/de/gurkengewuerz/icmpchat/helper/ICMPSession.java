package de.gurkengewuerz.icmpchat.helper;

import de.gurkengewuerz.icmpchat.object.Device;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.IpV4Helper;
import org.pcap4j.util.MacAddress;
import org.pmw.tinylog.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPSession {
    private PcapHandle handle;
    private PcapHandle readHandle;
    private Device device;

    private MacAddress ownMac = null;
    private InetAddress thisIP = null;
    private short seq = 0;

    public ICMPSession(PcapHandle handle, PcapHandle readHandle, Device device) {
        this.handle = handle;
        this.readHandle = readHandle;
        this.device = device;
    }

    public void sendICMP(InetAddress target, byte[] data) {
        MacAddress destMac = null;
        if (sameNetwork(target, device.getOwnIPv4(), device.getNetmask().toString().replace("/", ""))) { // Inside Network
            destMac = ARPCache.get(target, device, handle, readHandle);
        } else { // Outgoing
            // TODO: Get MAC from Gateway
        }
        if (destMac == null) {
            Logger.error("Cant Resolve Mac");
            return;
        }
        Logger.info("ICMP to " + target + " with MAC " + DatatypeConverter.printHexBinary(destMac.getAddress()));
        //create ICMP packet
        seq++;
        IcmpV4EchoPacket.Builder echoBuilder = new IcmpV4EchoPacket.Builder();
        echoBuilder
                .identifier((short) 1)
                .payloadBuilder(new UnknownPacket.Builder().rawData(data));

        IcmpV4CommonPacket.Builder icmpV4CommonBuilder = new IcmpV4CommonPacket.Builder();
        icmpV4CommonBuilder
                .type(IcmpV4Type.ECHO)
                .code(IcmpV4Code.NO_CODE)
                .payloadBuilder(echoBuilder)
                .correctChecksumAtBuild(true);


        IpV4Packet.Builder ipV4Builder = new IpV4Packet.Builder();
        ipV4Builder
                .version(IpVersion.IPV4)
                .tos(IpV4Rfc791Tos.newInstance((byte) 0))
                .ttl((byte) 100)
                .protocol(IpNumber.ICMPV4)
                .srcAddr((Inet4Address) device.getOwnIPv4())
                .dstAddr((Inet4Address) target)
                .payloadBuilder(icmpV4CommonBuilder)
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true);

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder.dstAddr(destMac)
                .srcAddr(device.getOwnMac())
                .type(EtherType.IPV4)
                .paddingAtBuild(true);

        echoBuilder.sequenceNumber(seq);
        ipV4Builder.identification(seq);

        for (final Packet ipV4Packet : IpV4Helper.fragment(ipV4Builder.build(), 1403)) {
            etherBuilder.payloadBuilder(
                    new AbstractPacket.AbstractBuilder() {
                        @Override
                        public Packet build() {
                            return ipV4Packet;
                        }
                    }
            );

            Packet p = etherBuilder.build();
            try {
                handle.sendPacket(p);
            } catch (PcapNativeException | NotOpenException e) {
                Logger.error(e);
            }
        }
    }

    public PcapHandle getHandle() {
        return handle;
    }

    public Device getDevice() {
        return device;
    }

    public static boolean sameNetwork(InetAddress ip1, InetAddress ip2, String mask) {
        try {
            byte[] a1 = ip1.getAddress();
            byte[] a2 = ip2.getAddress();
            byte[] m = InetAddress.getByName(mask).getAddress();

            for (int i = 0; i < a1.length; i++)
                if ((a1[i] & m[i]) != (a2[i] & m[i]))
                    return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;

    }
}
