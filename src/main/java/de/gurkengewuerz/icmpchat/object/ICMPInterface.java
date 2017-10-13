package de.gurkengewuerz.icmpchat.object;

import org.pcap4j.core.PcapNetworkInterface;

/**
 * Created by gurkengewuerz.de on 13.10.2017.
 */
public class ICMPInterface {
    private String key;
    private PcapNetworkInterface index;

    public ICMPInterface(String key, PcapNetworkInterface index)
    {
        this.key = key;
        this.index = index;
    }

    @Override
    public String toString()
    {
        return key;
    }

    public String getKey()
    {
        return key;
    }

    public PcapNetworkInterface getValue() {
        return index;
    }
}
