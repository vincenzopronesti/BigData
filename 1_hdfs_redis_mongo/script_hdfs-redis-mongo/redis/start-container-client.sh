#!/bin/bash
docker run --rm --network=redis_network -it sickp/alpine-redis redis-cli -h redis-server
