FROM ciscocloud/haproxy-consul
MAINTAINER Brandon Okert

RUN rm -rf /consul-template

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

# Haproxy + Consul Template
RUN apk --update add rsyslog grep \
  && mkdir -p /etc/haproxy \
  && chmod 755 /usr/bin/health-check.sh
