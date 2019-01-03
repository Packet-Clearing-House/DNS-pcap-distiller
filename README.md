# DNS-pcap-distiller

DNS pcap distiller (DPD) java application to grab DNS packets and write them to a file in the [DNSAuth log format](https://github.com/Packet-Clearing-House/DNSAuth#logs). 

## Installation and Running

### Prerequisites

* [Java 8](https://openjdk.java.net/install/)
* [Jpcap](https://github.com/mgodave/Jpcap) (see notes below)


#### Jpcap
This project depends on [Jpcap](https://github.com/mgodave/Jpcap) and its JNI library libjpcap. Refer to the Jpcap documentation for information on building the library for your system.

Note for MacOS X users: You may need to edit the JNI_INCLUDE2 variable in src/main/c/Makefile. The snippet below should work.
```
ifeq ($(PLATFORM), Darwin)
    JNI_INCLUDE2 = $(JAVA_DIR)/include/darwin 
    COMPILE_OPTION = -bundle -framework JavaVM
    SUFFIX = .jnilib
```

### Installation 

#### Via Compiling

1. Install prerequisites per above
1. Clone this repo ``git clone https://github.com/Packet-Clearing-House/DNS-pcap-distiller``
1. Edit the config file ``src/main/resources/application.yml``
1. Compile and generate the executable jar ``mvn package``
1. Start DNS-pcap-distiller: ``java -jar target/dns-pcap-distiller-1.0.0.jar``

Note that the maven build file assumes that the Jpcap repository has been cloned into a sibling directory. You will need to set the ``jpcap.dir`` property if this assumption does not hold.

#### Via downloading pre-compiled .jar

1. Install prerequisites per above
1. Go to [the DPD website](https://pch.net/dpd) and download the latest version
1. Download the config file [from github](https://raw.githubusercontent.com/Packet-Clearing-House/DNS-pcap-distiller/master/src/main/resources/application.yml) and edit it to your match your environment
1. Start DNS-pcap-distiller: ``java -jar dns-pcap-distiller-1.0.0.jar``

Note - You will need to gzip and send these files to your [DNSAuth](https://github.com/Packet-Clearing-House/DNSAuth) instance.

## Development

We welcome pull requests! Please fork this repository, test your code locally, commit it and open a pull request.

### Ubuntu Dev Quick Start

We test using Ubuntu 16.04, an endless loop bash script to simulate client DNS queries and an instance of [Pi-Hole](https://pi-hole.net/) to receive and respond to queries. To bootstrap your dev environment you can run:

```bash
curl -sSL https://raw.githubusercontent.com/Packet-Clearing-House/DNS-pcap-distiller/master/dev/ubuntu16DevProvision.sh | bash
```

If you want to inspect the contents of this bash script, feel free to manually copy it [from here](https://github.com/Packet-Clearing-House/DNS-pcap-distiller/blob/master/dev/ubuntu16DevProvision.sh) and review before running it.

The script allows you to send ~5 queries/second by default. Assuming your name server is 192.168.1.1, that'd look like this:

```bash
./ubuntu16DevProvision.sh 192.168.1.1
```

There's a sleep and multiplier option too.  Sleep defaults to 1.0 seconds and the multiplier defaults to 1x. Here's two other examples:

```bash
./lotsOfDnsQueries.sh 192.168.1.1 0.5   #  0.5 sleep, 1x multiplier 

./lotsOfDnsQueries.sh 192.168.1.1 0 100 #  0 sleep, 100x multiplier
```

If you need to see queries and responses in real time to debug, us this ``tcpdump`` command:

```
tcpdump -l -nttttv -i any  port 53 and not dst 9.9.9.9 and not src 9.9.9.9
```

## Troubleshooting

### UnknownHostException
The application may throw an error when attempting to resolve the local host name. The following message can be ignored.
```
java.net.UnknownHostException: <hostname>: <hostname>: Name or service not known
        at java.net.InetAddress.getLocalHost(...)
        ...
```

### StringIndexOutOfBoundsException
If you see an error like:
```
Caused by: java.lang.StringIndexOutOfBoundsException: String index out of range: -1
at java.lang.String.substring(String.java:1967) ~[na:1.8.0_181]
at net.pch.dns.pcap.distiller.Application.run(Application.java:44) [classes!/:1.0.0]
at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:809) [spring-boot-2.0.5.RELEASE.jar!/:2.0.5.RELEASE]
```
Then ensure you're using FQDN format for hosts.  See [#2](https://github.com/Packet-Clearing-House/DNS-pcap-distiller/issues/2) for details

## License
DNS-pcap-distiller is licensed under MIT.
