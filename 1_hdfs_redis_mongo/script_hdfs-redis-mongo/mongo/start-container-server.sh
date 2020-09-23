#!/bin/bash
docker network create mongonet
docker run --rm -t -i -p 27017:27017 --network=mongonet --name mongo_server mongo /usr/bin/mongod --bind_ip_all
