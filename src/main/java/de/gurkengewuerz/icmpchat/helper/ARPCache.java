package de.gurkengewuerz.icmpchat.helper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.gurkengewuerz.icmpchat.object.Device;
import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;
import org.pmw.tinylog.Logger;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ARPCache {

    private static Cache<InetAddress, MacAddress> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    public static MacAddress get(InetAddress target, Device device, PcapHandle handle, PcapHandle readHandle) {
        MacAddress macInCache = cache.getIfPresent(target);
        if (macInCache != null) {
            Logger.info("Get ARP from cache for " + target.getHostAddress());
            return macInCache;
        }
        Logger.info("Do ARP for " + target.getHostAddress());
        ExecutorService pool = Executors.newSingleThreadExecutor();

        ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
        arpBuilder
                .hardwareType(ArpHardwareType.ETHERNET)
                .protocolType(EtherType.IPV4)
                .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                .protocolAddrLength((byte) ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
                .operation(ArpOperation.REQUEST)
                .srcHardwareAddr(device.getOwnMac())
                .srcProtocolAddr(device.getOwnIPv4())
                .dstHardwareAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .dstProtocolAddr(target);

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder.dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .srcAddr(device.getOwnMac())
                .type(EtherType.ARP)
                .payloadBuilder(arpBuilder)
                .paddingAtBuild(true);

        Packet p = etherBuilder.build();
        try {
            final MacAddress[] answer = {null};

            readHandle.setFilter(
                    "arp",
                    BpfProgram.BpfCompileMode.OPTIMIZE
            );

            PacketListener listener = packet -> {
                if (packet.contains(ArpPacket.class)) {
                    ArpPacket arp = packet.get(ArpPacket.class);
                    if (arp.getHeader().getOperation().equals(ArpOperation.REPLY)) {
                        answer[0] = arp.getHeader().getSrcHardwareAddr();
                    }
                }
            };
            Task t = new Task(readHandle, listener);
            pool.execute(t);


            handle.sendPacket(p);

            long start = System.currentTimeMillis();
            int TIMEOUT = 1 * 1000;
            while (System.currentTimeMillis() - start <= TIMEOUT && answer[0] == null) {
            }
            pool.shutdown();
            if (answer[0] != null) cache.put(target, answer[0]);
            return answer[0];
        } catch (PcapNativeException | NotOpenException e) {
            Logger.error(e);
        }
        return null;
    }

    private static class Task implements Runnable {

        private PcapHandle handle;
        private PacketListener listener;

        public Task(PcapHandle handle, PacketListener listener) {
            this.handle = handle;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                handle.loop(5, listener);
            } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                Logger.error(e);
            }
        }

    }
}
