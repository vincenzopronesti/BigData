#!/bin/bash
docker network create redis_network
docker run --rm --network=redis_network --name=redis-server sickp/alpine-redis
