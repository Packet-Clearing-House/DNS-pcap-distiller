package net.pch.dns.pcap.distiller;

import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

@SpringBootApplication
public class Application implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Collector collector;

    @Autowired
    private DirectoryProperties props;

    @Autowired
    private BlockingQueue<Packet> queue;

    @Override
    public void run(ApplicationArguments args) {

        // start the collector
        new Thread(collector, "Collector").start();
        boolean shutdown = false;

        int queueTotal = 0;
        int queueCount = 0;
        int avgQueueSize = 0;

        String hostname = "";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            hostname = hostname.substring(0, hostname.indexOf("."));
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            return;
        }

        while (!shutdown) {
             Processor processor = new Processor(queue, props, hostname);
             Thread t = new Thread(processor, "Processor");
             t.start();

                long t0 = System.currentTimeMillis();
                long t1 = t0 + (60 * 1000);

                // gather stats
                do {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int size = queue.size();
                    queueTotal += size; // this could overflow given specific configurations
                    queueCount++;

                } while (System.currentTimeMillis() < t1 && !shutdown);

                int interval = (int) ((System.currentTimeMillis() - t0) / 1000);
                if (queueTotal > 0) {
                    avgQueueSize = queueTotal / queueCount;
                } else {
                    avgQueueSize = 0;
                }
                processor.setDone(interval, queueTotal, avgQueueSize);
                try {
                    t.join(2 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

}