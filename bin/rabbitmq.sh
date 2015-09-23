#!/bin/bash

docker run -d --hostname cna-rabbit -p 5672:5672 -d rabbitmq:3

docker run -d -p 15672:15672 rabbitmq:3-management
