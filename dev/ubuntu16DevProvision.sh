#!/bin/bash

apt-get update
apt-get upgrade -y
apt-get install -y openjdk-8-jre unzip zip git
git clone https://github.com/Packet-Clearing-House/DNS-pcap-distiller.git
cd DNS-pcap-distiller
git checkout WEB-1158
cd dev
unzip jplibpcap.so.zip
mv libjpcap.so /usr/lib/
cd
# thanks https://stackoverflow.com/a/21336679 !
my_ip=$(ip route get 9.9.9.9 | awk 'NR==1 {print $NF}')
wget -O pi-hole-basic-install.sh https://install.pi-hole.net
chmod +x pi-hole-basic-install.sh
mkdir /etc/pihole/
cat > /etc/pihole/setupVars.conf <<EOF
INSTALL_WEB=false
PIHOLE_DNS_1=9.9.9.9
PIHOLE_INTERFACE=eth0
IPV4_ADDRESS=$my_ip
QUERY_LOGGING=false
EOF
./pi-hole-basic-install.sh --unattended

echo ""
echo "Done!"
echo ""
echo "Run this to simulate queries:"
echo ""
echo "  ./lotsOfDnsQueries.sh $my_ip"
echo ""