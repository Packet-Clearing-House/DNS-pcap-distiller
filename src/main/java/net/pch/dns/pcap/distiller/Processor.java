package net.pch.dns.pcap.distiller;

import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * The Processor pulls entries from the queue of messages and determines if it is tcp/udp and a message in which we are interested and
 * written to a file.
 */
public class Processor implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(Processor.class);
    private int queueMax;
    private int queueAvg;

    public Processor(BlockingQueue<Packet> queue, DirectoryProperties props, String hostname) {
        this.queue = queue;
        this.props = props;
        this.hostname = hostname;
    }

    BlockingQueue<Packet> queue;

    DirectoryProperties props;
    boolean done = false;
    String hostname;
    String version = "1.0";

    @Override
    public void run() {
        int goodPackets = 0;
        int badPackets = 0;

        String startTime = new SimpleDateFormat("yyyy-MM-dd.HH-mm").format(new Date());
        String tmpFilename = String.format("%s/SZC_%s_%s.tmp", props.temp, hostname, startTime);
        logger.debug("Writing to temp file {}", tmpFilename);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(tmpFilename));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        out.format("# DNSSimpleCollector\n# Version: %s\n# Host: %s\n# Start: %s\n", version, hostname, startTime);

        try {
            while (!done) {
                Packet packet = queue.take();
                try {
                    byte[] data = packet.data;
                    if (data.length < 10)
                        continue;

                    int proto = 0;
                    if (packet instanceof TCPPacket) {
                        data = Arrays.copyOfRange(data, 2, data.length);
                        proto = 1;
                    }

                    Message message = new Message(data);
                    Header header = message.getHeader();
                    int opCode = header.getOpcode();
                    String srcIp = ((IPPacket) packet).src_ip.getHostAddress();
                    String dstIp = ((IPPacket) packet).dst_ip.getHostAddress();

                    PrintWriter o = out;

                    Record[] records = message.getSectionArray(Section.QUESTION);
                    if (records.length > 0) {
                        String zoneName = records[0].getName().toString().toLowerCase();
                        int type = records[0].getType();
                        if (!header.getFlag(Flags.QR)) {
                            o.format("Q %s %s %d %d %d %s %d\n", srcIp, dstIp, proto, opCode, type, zoneName, data.length);
                        } else {    // if response grab response code and reverse src/dst IPs
                            int rCode = header.getRcode();
                            o.format("R %s %s %d %d %d %s %d %d\n", dstIp, srcIp, proto, opCode, type, zoneName, data.length, rCode);
                        }
                    }
                    goodPackets++;
                } catch (IllegalArgumentException | IOException e) {
                    logger.debug("bad packet");
                    badPackets++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String endTime = new SimpleDateFormat("yyyy-MM-dd.HH-mm").format(new Date());
        String filename = String.format("%s/SZC_%s_%s.dmp", props.data, hostname, endTime);

        out.format("# End: %s\n# Queue avg: %d, max: %d\n# Memory usage %dM of %dM max(%dM)\n# Packets: %d good, %d bad\n",
                endTime, this.queueAvg, this.queueMax,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024,
                Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024, goodPackets, badPackets);
        out.flush();
        out.close();

        (new File(tmpFilename)).renameTo(new File(filename));
        logger.info("Wrote file {}", filename);
    }

    /**
     * Marks this Processor as done. This does not interrupt the processor; the main loop will process the next packet (whenever that is) before finalizing.
     */
    public void setDone(long duration, int queueMax, int queueAvg) {
        // TODO: what is duration used for?
        this.queueMax = queueMax;
        this.queueAvg = queueAvg;
        done = true;
    }
}
