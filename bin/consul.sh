#!/bin/bash
### NB!! you'll need to add an entry to your local /etc/hosts (not that of the Docker container!) of the following form:
###      127.0.0.1         cna
### in order for this to work.

# --add-host cna:10.0.2.2
docker run -p 8400:8400 -p 8500:8500 -p 8600:53/udp   -h cna progrium/consul -server -bootstrap -ui-dir /ui
