#!/bin/bash
if [ $# -eq 0 ]
  then
    echo ""
    echo "No IP supplied. Signature is:"
    echo ""
    echo "./lotsOfDnsQueries.sh NS_IP [SLEEP] [MULTIPLIER]"
    echo ""
    echo "  NS_IP - the IP of your nameserver"
    echo "  SLEEP - optionally sleep. Defaults to 1.0 second. Set to <1 (eg 0.5 or 0) to speed up"
    echo "  MULTIPLIER - optionally multiply. Defaults to 1 iteration. Set to >1 (eg 100) to speed up"
    echo ""
    echo "./lotsOfDnsQueries.sh 192.168.1.1       #  queries to 192.168.1.1, 1.s sleep, 1x multiplier"
    echo ""
    echo "./lotsOfDnsQueries.sh 192.168.1.1 0.5   #  queries to 192.168.1.1, 0.5 sleep, 1x multiplier "
    echo ""
    echo "./lotsOfDnsQueries.sh 192.168.1.1 0 100 # queries to 192.168.1.1, 0 sleep, 100x multiplier"
    echo ""


else
    SLEEP=$2
    : ${SLEEP:=1.0}

    MULTIPLIER=$3
    : ${MULTIPLIER:=1}

    DNS_IP=$1
    echo ""
    echo "I will now send endless queries to $DNS_IP until you stop me!"
    echo ""
    declare -a ZONES=("pch.net" "www.pch.net" "lg.pch.net" "prefix.pch.net" "quad9.net" "www.quad9.net")
    TYPES[0]="A"
    TYPES[1]="AAAA"
    TYPES[2]="TXT"
    PROTOS[0]="" # default is udp so empty string means UDP
    PROTOS[1]="+tcp"
    while true; do
            sleep $SLEEP
            for ZONE in "${ZONES[@]}"
            do
                index=$[$RANDOM % ${#TYPES[@]}]
                TYPE=${TYPE[$index]}
                index=$[$RANDOM % ${#PROTOS[@]}]
                PROTO=${PROTOS[$index]}
                for run in {1..MULTIPLIER}
                do
                  (/usr/bin/dig ${PROTO} @${DNS_IP} ${TYPE} ${ZONE} +short > /dev/null)&
                done
            done
            wait # allows sub-shells from the dig call to catch up - we error out otherwise with large multiplier
    done

fi
