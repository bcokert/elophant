#!/usr/bin/env bash
FAILED=false

if ! sv check elophant-haproxy | grep -q "ok: run"; then
  echo "elophant-haproxy is not running: $(sv check elophant-haproxy)"
  echo "Log tail: "
  echo $(tail -5 /var/log/elophant-haproxy/current)
  FAILED=true
fi

echo

if ! sv check elophant-rsyslog | grep -q "ok: run"; then
  echo "elophant-rsyslog is not running: $(sv check elophant-rsyslog)"
  echo "Log tail: "
  echo $(tail -5 /var/log/elophant-rsyslog/current)
  FAILED=true
fi

if [ ${FAILED} = true ]; then
  exit 2
else
  exit 0
fi
