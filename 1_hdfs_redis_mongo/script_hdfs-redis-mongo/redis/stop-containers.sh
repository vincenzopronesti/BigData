#!/bin/bash
docker kill eager-archimedes
docker kill redis-server
docker network remove redis_network
