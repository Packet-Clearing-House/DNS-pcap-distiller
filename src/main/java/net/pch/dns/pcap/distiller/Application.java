package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xbill.DNS.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

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
            byte[] data = null;
            try {
                data = packet.data;
                System.err.printf("new packet %d bytes\n", data.length);
                if (data.length < 10)
                    return;

                int proto = 0;
                if (packet instanceof TCPPacket) {
                    data = Arrays.copyOfRange(data, 2, data.length);
                    proto = 1;
                }

                //System.err.println(new String(Base64.getEncoder().encode(data)));

                Message message = new Message(data);
                Header header = message.getHeader();
                int opCode = header.getOpcode();
                String srcIp = ((IPPacket) packet).src_ip.getHostAddress();
                String dstIp = ((IPPacket) packet).dst_ip.getHostAddress();

                Record[] records = message.getSectionArray(Section.QUESTION);
                if (records.length > 0) {
                    String zoneName = records[0].getName().toString().toLowerCase();
                    int type = records[0].getType();
                    if (header.getFlag(Flags.QR) == false) {
                        System.out.format("Q %s %s %d %d %d %s %d\n", srcIp, dstIp, proto, opCode, type, zoneName, data.length);
                    } else {    // if response grab response code and reverse src/dst IPs
                        int rCode = header.getRcode();
                        System.out.format("R %s %s %d %d %d %s %d %d\n", dstIp, srcIp, proto, opCode, type, zoneName, data.length, rCode);
                    }
                    System.out.flush();
                }
            } catch (WireParseException ex) {
                if (ex.getMessage().contains("compression")) {
                    ex.printStackTrace(System.err);
                    System.err.println(new String(Base64.getEncoder().encode(data)));
                }
            } catch (IOException e) {
                System.err.println("io exceptions!" + e);
            }
        });

        captor.close();
    }

}