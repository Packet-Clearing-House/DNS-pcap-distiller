package net.pch.dns.pcap.distiller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "distiller.directory")
public class DirectoryProperties {

    String temp;

    String data;

}
