package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Data
@Component
@ConfigurationProperties(prefix = "distiller")
public class CaptorFactory {

    // the interface to liston on
    private String intf;

    // the snapshot length
    private Integer snaplen;

    // true if the captor should bind in promiscuous mode
    private Boolean promiscuous;

    /**
     * Confirms that the libjpcap shared library is accessible to the JVM and the configured interface exists.
     * Refer to the README for information on the installation the libjpcap library.
     */
    @PostConstruct
    public void verifyInterfaceConfiguration () {
        if (getNetworkInterface() == null) {
            throw new RuntimeException(String.format("no interface %s", intf));
        }
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
    public JpcapCaptor newCaptor() throws IOException {
        int timeout = 1000; // this has no effect given we use loopPacket
        return JpcapCaptor.openDevice(getNetworkInterface(), snaplen, promiscuous, timeout);
    }

}
