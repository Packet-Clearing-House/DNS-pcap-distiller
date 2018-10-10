package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xbill.DNS.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Base64;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public Application(CaptureConfiguration captureConfiguration) throws Exception {

        JpcapCaptor captor = captureConfiguration.getCaptor();

        // set filters
        // this should move behind a configuration
        captor.setFilter("port 53", true);

        PrintStream o = System.out;

        int forever = -1;
        captor.loopPacket(forever, (Packet packet) -> {
            byte[] data = null;
            try {
                data = packet.data;

                // We're seeing TCP SYNs, ACKs, and FINs.
                // They're packets matching our filter.
                // Discard non-DNS data
                if (data.length < 10)
                    return;

                int proto = 0;
                if (packet instanceof TCPPacket) {
                    // the first two bytes are the length field
                    // we could use this to figure out if the message is fragmented
                    // TODO: investigate performance by replacing with wrapped ByteBuf for DNSInput
                    data = Arrays.copyOfRange(data, 2, data.length);
                    proto = 1;
                }

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
                        o.format("Q %s %s %d %d %d %s %d\n", srcIp, dstIp, proto, opCode, type, zoneName, data.length);
                    } else {    // if response grab response code and reverse src/dst IPs
                        int rCode = header.getRcode();
                        o.format("R %s %s %d %d %d %s %d %d\n", dstIp, srcIp, proto, opCode, type, zoneName, data.length, rCode);
                    }
                }
            } catch (WireParseException ex) {
                if (ex.getMessage().contains("compression")) {
                    ex.printStackTrace(System.err);
                    System.err.println(new String(Base64.getEncoder().encode(data)));
                }
            } catch (Throwable e) {
                System.err.println("exceptions!" + e);
            }
        });

        captor.close();
    }

}