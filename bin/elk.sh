#!/bin/bash
docker run --name cna-elk   -p 80:80 -p 2003:2003 -p 8125:8125/udp hopsoft/graphite-statsd
