#!/usr/bin/env sh

consul agent -config-file /etc/consul.d/client/elophant_consul_client.json -node ${CONSUL_NODE_NAME} -retry-interval 5s -retry-join ${CONSUL_SERVERS}
