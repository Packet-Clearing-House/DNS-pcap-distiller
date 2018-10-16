package net.pch.dns.pcap.distiller;

import jpcap.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provides the capacity-limited queue.
 */
@Configuration
@ConfigurationProperties(prefix = "distiller.queue")
public class Queue {

    @Getter
    @Setter
    private int maxSize = 20000;

    @Bean
    public BlockingQueue<Packet> getQueue() {
        return new LinkedBlockingQueue<>(maxSize);
    }

}
