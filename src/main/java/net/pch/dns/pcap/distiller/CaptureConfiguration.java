package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class CaptureConfiguration {

    @Value("${distiller.interface}")
    public String intf = "en0";

    @Value("${distiller.snaplen}")
    public Integer snaplen = 65535;

    @Value("${distiller.promiscuous}")
    public Boolean promiscuous = true;

    /**
     * Returns true if this application can link to the libjpcap library.
     * A return value of false suggests that the libjpcap shared library is not accessible to the JVM.
     * Refer to the README for information on the installation the libjpcap library.
     */
    public boolean canResolveLibrary() {
        try {
            JpcapCaptor.getDeviceList();
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the NetworkInterface for the given configuration or <code>null</code> if no interface is found.
     * The interface name must be an exact match, i.e. EN0 will not match en0.
     */
    private NetworkInterface getNetworkInterface() {
        NetworkInterface device = null;
        for (NetworkInterface s : JpcapCaptor.getDeviceList()) {
            if (intf.equals(s.name)) {
                device = s;
            }
        }
        return device;
    }

    /**
     * Returns a captor for the given configuration.
     */
    public JpcapCaptor getCaptor() throws IOException {
        int timeout = 1000; // this has no effect given we use loopPacket
        return JpcapCaptor.openDevice(getNetworkInterface(), snaplen, promiscuous, timeout);
    }

}
