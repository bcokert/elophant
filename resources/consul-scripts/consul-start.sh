#!/usr/bin/env sh

sleep 5

CO_SERVER_1=$(cat /etc/hosts | grep elophant_consul_$(( ((CONSUL_NODE_NUMBER) % 5)+1 )).${NETWORK} | cut -f 1)
CO_SERVER_2=$(cat /etc/hosts | grep elophant_consul_$(( ((CONSUL_NODE_NUMBER+1) % 5)+1 )).${NETWORK} | cut -f 1)
CO_SERVER_3=$(cat /etc/hosts | grep elophant_consul_$(( ((CONSUL_NODE_NUMBER+2) % 5)+1 )).${NETWORK} | cut -f 1)
CO_SERVER_4=$(cat /etc/hosts | grep elophant_consul_$(( ((CONSUL_NODE_NUMBER+3) % 5)+1 )).${NETWORK} | cut -f 1)

consul agent -config-file /etc/consul.d/server/elophant_consul_server.json -node "elophant_consul_${CONSUL_NODE_NUMBER}" -retry-interval 5s -retry-join ${CO_SERVER_1} ${CO_SERVER_2} ${CO_SERVER_3} ${CO_SERVER_4}
