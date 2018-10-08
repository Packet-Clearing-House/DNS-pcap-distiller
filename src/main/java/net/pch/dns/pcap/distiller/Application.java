package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public Application() throws Exception {
        NetworkInterface device = null;
        for (NetworkInterface s : JpcapCaptor.getDeviceList()) {
            if ("en0".equals(s.name) || "eth0".equals(s.name)) {
                device = s;
            }
        }

        int snaplen = 65535; // see https://wiki.wireshark.org/SnapLen
        boolean promiscuous = true;
        int timeout = 1000; // TODO: how does this value influence the system?
        JpcapCaptor captor = JpcapCaptor.openDevice(device, snaplen, promiscuous, timeout);

        // set filters
        // we could set ip-based filters here
        captor.setFilter("port 53", true);

        int forever = -1;
        captor.loopPacket(forever, (Packet packet) -> {
            System.out.println("received packet");
        });

        captor.close();
    }

}