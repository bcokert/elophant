FROM gliderlabs/consul
MAINTAINER Brandon Okert

# Runit Config
COPY etc /etc
COPY usr /usr
RUN echo "@testing http://dl-4.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories \
  && apk --update add bash vim curl tzdata runit@testing \
  && chmod -R 755 /etc/service/ \
  && chmod 755 /usr/sbin/runit_bootstrap \
  && chmod 755 /usr/sbin/runsvdir-start \
  && touch /etc/inittab \
  && cp /usr/share/zoneinfo/Canada/Pacific /etc/localtime \
  && echo "Canada/Vancouver" > /etc/timezone \
  && apk del tzdata
VOLUME /var/log
ENTRYPOINT []
CMD ["/usr/sbin/runit_bootstrap"]

# Consul Client Config
ENV GOMAXPROCS 2
EXPOSE 8300 8301 8301/udp 8302 8302/udp 8400 8500 8600 8600/udp
VOLUME /data/consul
