#!/bin/bash

apt-get update
apt-get upgrade -y
# todo - ensure we install debs to avoid, "no jpcap in java.library.path" error 
apt-get install -y openjdk-8-jre libpcap0.8
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

echo ""
echo "Done!"
echo ""
echo "Run this to simulate queries:"
echo ""
echo "  ./lotsOfDnsQueries.sh $my_ip"
echo ""