# DNS-pcap-distiller

DNS pcap distiller java application to grab DNS packets and write them to a gzip file in the [DNSAuth log format](https://github.com/Packet-Clearing-House/DNSAuth#logs). 

## Installation and Running

### Prerequisites

* [Java 8](https://openjdk.java.net/install/)
* A server that can see the DNS packets you wish to capture
* An interface named ``eth0`` 

### Quick Start
TBD


## Development

We welcome pull requests! Please fork this repository, test your code locally, commit it and open a pull request.

### Ubuntu Dev Quick Start

We test using Ubuntu 16.04, an endless loop bash script to simulate client DNS queries and an instance of [Pi-Hole](https://pi-hole.net/) to receive and respond to queries. To bootstrap your dev environment:

```
apt-get update
apt-get upgrade
apt-get install -y openjdk-8-jre
wget -O pi-hole-basic-install.sh https://install.pi-hole.net
chmod +x pi-hole-basic-install.sh
mkdir /etc/pihole/
# thanks https://stackoverflow.com/a/21336679 !
my_ip=$(ip route get 9.9.9.9 | awk 'NR==1 {print $NF}')
cat > /etc/pihole/setupVars.conf <<EOF
INSTALL_WEB=false 
PIHOLE_DNS_1=9.9.9.9
PIHOLE_INTERFACE=eth0
IPV4_ADDRESS=$my_ip
QUERY_LOGGING=false
EOF
./pi-hole-basic-install.sh --unattended
echo "on another machine, run:     ./lotsOfDnsQueries.sh $my_ip"
```

If you need to see queries and responses in real time to debug, us this ``tcpdump`` command, replacing 9.9.9.9 with your upstream recursive resolver:

```
tcpdump -l -nttttv -i eth0  port 53 and not dst 9.9.9.9 and not src 9.9.9.9
```




## License
DNS-pcap-distiller is licensed under MIT.
