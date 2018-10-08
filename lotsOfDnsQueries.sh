#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "No IP supplied"

else

    DNS_IP=$1
    echo ""
    echo "I will now send endless queries to $DNS_IP until you stop me!"
    echo ""
    declare -a ZONES=("pch.net" "www.pch.net" "lg.pch.net" "prefix.pch.net" "com")
    TYPES[0]="A"
    TYPES[1]="AAAA"
    TYPES[2]="TXT"
    PROTOS[0]="" # default is udp so empty string means UDP
    PROTOS[1]="+tcp"
    while true; do
            sleep 1.01 & # change this to be less or more to go slower or faster
            for ZONE in "${ZONES[@]}"
            do
                index=$[$RANDOM % ${#TYPES[@]}]
                TYPE=${TYPE[$index]}
                index=$[$RANDOM % ${#PROTOS[@]}]
                PROTO=${PROTOS[$index]}
                `/usr/bin/dig ${PROTO} @${DNS_IP} ${TYPE} ${ZONE} +short > out.log 2> /dev/null`
            done
            wait # for sleep
    done

fi