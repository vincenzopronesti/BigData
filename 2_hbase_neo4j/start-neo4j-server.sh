#!/bin/bash
DATA_DIR=$(pwd)/volume
docker run --rm \
	--publish=7474:7474 --publish=7687:7687 \
	--volume=$DATA_DIR/neo4j:/data \
	neo4j:3.0

