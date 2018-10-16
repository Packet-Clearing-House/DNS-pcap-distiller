package net.pch.dns.pcap.distiller;

import jpcap.JpcapCaptor;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

@Component
public class Collector implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Collector.class);

    @Autowired
    private CaptorFactory factory;

    @Autowired
    private BlockingQueue<Packet> queue;

    @Override
    public void run() {
        while(true) {
            try {
                JpcapCaptor captor = factory.newCaptor();

                captor.setFilter("port 53", true);

                final int forever = -1; // for readability
                captor.loopPacket(forever, (packet) -> {

                    // TODO: what non-ip packets are arriving on port 53? can we push this down to the filter?
                    if(packet instanceof IPPacket) {
                        if(!queue.offer(packet)) {
                            logger.debug("Packet dropped; queue too large");
                        }
                    } else {
                        logger.debug("Packet is not an IPPacket\n" + packet);
                    }

                });

                captor.close();
                logger.info("Jpcap loopPacket terminated");
            } catch (IOException e) {
                logger.error("Exception in captor", e);
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
    }

}
