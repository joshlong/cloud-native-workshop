#!/bin/bash

redis_container_id=$( docker ps -a | grep redis | cut -f1 -d\ )


if [ "$redis_container_id" = "" ] ;
then
  docker run --name redis -p 6379:6379 redis
else
  echo "we found a container called 'redis' with this container ID: $redis_container_id."
  docker start $redis_container_id
fi
