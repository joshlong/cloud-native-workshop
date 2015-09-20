#!/bin/bash
docker run --name cna-elk  -e "LOGSTASH_CONFIG_URL=https://raw.githubusercontent.com/mstine/statsd-graphite-demo/master/logstash.conf"  -p 10042:10042 -p 9292:9292 -p 9200:9200 pblittle/docker-logstash
