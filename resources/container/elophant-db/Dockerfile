FROM postgres:9.4
MAINTAINER Brandon Okert

# Runit Config
COPY etc /etc
COPY usr /usr
RUN apt-get update \
  && apt-get install -y bash vim runit curl \
  && chmod -R 755 /etc/service/ \
  && chmod 755 /usr/sbin/runit_bootstrap \
  && chmod 755 /usr/sbin/runsvdir-start \
  && touch /etc/inittab
VOLUME /var/log
ENTRYPOINT []
CMD ["/usr/sbin/runit_bootstrap"]

# Database
RUN chmod -R 755 /usr/bin/db-init/
