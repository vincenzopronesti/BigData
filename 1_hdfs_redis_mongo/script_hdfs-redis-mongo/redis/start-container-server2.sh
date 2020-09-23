#!/bin/bash
docker network create redis_network
docker run --rm --network=redis_network --name=redis-server -v tmp:/data sickp/alpine-redis
