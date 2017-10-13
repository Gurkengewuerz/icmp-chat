package de.gurkengewuerz.icmpchat.object;

import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.LinkLayerAddress;
import org.pcap4j.util.MacAddress;
import org.pmw.tinylog.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class Device {
    private PcapNetworkInterface device;
    private MacAddress ownMac;
    private InetAddress ownIPv4;
    private InetAddress gateway;
    private InetAddress netmask;

    public Device(PcapNetworkInterface device) {
        this.device = device;

        for (PcapAddress address : device.getAddresses()) {
            netmask = address.getNetmask();
            ownIPv4 = address.getAddress();
        }

        for (LinkLayerAddress address : device.getLinkLayerAddresses()) {
            if (address instanceof MacAddress) {
                ownMac = (MacAddress) address;
            }
        }

        Logger.info(DatatypeConverter.printHexBinary(ownMac.getAddress()) + " " + ownIPv4.getHostAddress());
    }

    public PcapNetworkInterface getDevice() {
        return device;
    }

    public MacAddress getOwnMac() {
        return ownMac;
    }

    public InetAddress getOwnIPv4() {
        return ownIPv4;
    }

    public InetAddress getGateway() {
        return gateway;
    }

    public InetAddress getNetmask() {
        return netmask;
    }
}
