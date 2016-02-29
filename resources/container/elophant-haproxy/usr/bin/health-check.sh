#!/usr/bin/env bash
FAILED=false

if ! sv check elophant-haproxy | grep -q "ok: run"; then
read -r -d '' CHECK1 <<- EOM
elophant-haproxy is not running: $(sv check elophant-haproxy)
Log tail:
$(tail -5 /var/log/elophant-haproxy/current)
EOM
FAILED=true
fi

if ! sv check elophant-rsyslog | grep -q "ok: run"; then
read -r -d '' CHECK2 <<- EOM
elophant-rsyslog is not running: $(sv check elophant-rsyslog)
Log tail:
$(tail -5 /var/log/elophant-rsyslog/current)
EOM
FAILED=true
fi

ALLCHECKS="${CHECK1}\n${CHECK2}"

if [ ${FAILED} = true ]; then
  echo -ne "HTTP/1.1 500 FAILED\n\n${ALLCHECKS}\n"
else
  echo -ne "HTTP/1.1 200 OK\n\nAll Checks Passed\n"
fi
