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
    declare -a TYPES=("A" "AAAA" "TXT")
    declare -a PROTOS=("" "+tcp") # default is udp so empty string means UDP
    while true; do
            sleep 1.01 & # change this to be less or more to go slower or faster
            for ZONE in "${ZONES[@]}"
            do
                # todo - first type and first proto are selected
                TYPE=${TYPES[$RANDOM % ${#RANDOM[*]}]}
                PROTO=${PROTOS[$RANDOM % ${#RANDOM[*]}]}
                `/usr/bin/dig ${PROTO} @${DNS_IP} ${TYPE} ${ZONE} +short > out.log 2> /dev/null`
    #           echo "dig ${PROTO} @${DNS_IP} ${TYPE} ${ZONE} +short" # debug
            done
            wait # for sleep
    done

fi