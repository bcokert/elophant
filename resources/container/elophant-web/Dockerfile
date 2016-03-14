FROM java:8-jdk
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
  && touch /etc/inittab
VOLUME /var/log
ENTRYPOINT []
CMD ["/usr/sbin/runit_bootstrap"]

# Server
WORKDIR /usr/local/lib
EXPOSE 9000
COPY elophant-web elophant-web/
VOLUME /usr/local/lib/elophant-web/web/logs

