#!/bin/bash
docker build -t traffic-sim .
docker run --rm -v "$(pwd):/app/data" traffic-sim "/app/data/$1" "/app/data/$2"
