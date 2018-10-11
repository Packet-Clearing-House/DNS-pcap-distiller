# DNS-pcap-distiller

DNS pcap distiller java application to grab DNS packets and write them to a gzip file in the [DNSAuth log format](https://github.com/Packet-Clearing-House/DNSAuth#logs). 

## Installation and Running

### Prerequisites

* [Java 8](https://openjdk.java.net/install/)
* [Jpcap](https://github.com/mgodave/Jpcap) (see notes below)
* A server that can see the DNS packets you wish to capture
* An interface named ``eth0``


#### Jpcap
This project depends on [Jpcap](https://github.com/mgodave/Jpcap) and its JNI library libjpcap. Refer to the Jpcap documentation for information on building the library for your system.

Note for MacOS X users: I had to edit the JNI_INCLUDE2 variable in src/main/c/Makefile. The snippet below should work.
```
ifeq ($(PLATFORM), Darwin)
    JNI_INCLUDE2 = $(JAVA_DIR)/include/darwin 
    COMPILE_OPTION = -bundle -framework JavaVM
    SUFFIX = .jnilib
```

### Installation

This is a draft, but roughly:

1. Install prerequisites per above
1. Clone this repo ``git clone https://github.com/Packet-Clearing-House/DNS-pcap-distiller``
1. Edit your config file ``file_name_tbd``
1. Start DNS-pcap-distiller: ``java -jar dns-pcap-distiller-0.0.1-SNAPSHOT.jar``


## Development

We welcome pull requests! Please fork this repository, test your code locally, commit it and open a pull request.

The maven build file assumes that the Jpcap repository has been cloned into a sibling directory. You will need to set the ``jpcap.dir`` property if this assumption does not hold.

### Ubuntu Dev Quick Start

We test using Ubuntu 16.04, an endless loop bash script to simulate client DNS queries and an instance of [Pi-Hole](https://pi-hole.net/) to receive and respond to queries. To bootstrap your dev environment you can run:

```bash
curl -sSL https://raw.githubusercontent.com/Packet-Clearing-House/DNS-pcap-distiller/WEB-1158/dev/ubuntu16DevProvision.sh | bash
```

If you want to inspect the contents of this bash script, feel free to manually copy it [from here](https://github.com/Packet-Clearing-House/DNS-pcap-distiller/blob/WEB-1158/dev/ubuntu16DevProvision.sh) and review before running it.

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

## License
DNS-pcap-distiller is licensed under MIT.
