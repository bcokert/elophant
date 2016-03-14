FROM postgres:9.4
MAINTAINER Brandon Okert

# Runit Config
COPY etc /etc
COPY usr /usr
RUN apt-get update \
  && apt-get install -y bash vim runit curl \
  && chmod -R 755 /etc/service/ \
  && chmod -R 755 /etc/cron.d/ \
  && chmod 755 /usr/sbin/runit_bootstrap \
  && chmod 755 /usr/sbin/runsvdir-start \
  && touch /etc/inittab \
  && echo "America/Vancouver" > /etc/timezone \
  && dpkg-reconfigure -f noninteractive tzdata
VOLUME /var/log
ENTRYPOINT []
CMD ["/usr/sbin/runit_bootstrap"]

# Database
ENV AWS_CONFIG_FILE /etc/aws/config
RUN apt-get install -y awscli \
  && chmod -R 755 /usr/bin/db-init/ \
  && chmod -R 755 /usr/bin/db-maintenance/
