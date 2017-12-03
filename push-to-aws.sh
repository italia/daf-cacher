#!/bin/bash

docker tag italia/daf-metabase-cacher:latest 071817663189.dkr.ecr.us-west-1.amazonaws.com/daf-cacher:latest
docker push 071817663189.dkr.ecr.us-west-1.amazonaws.com/daf-cacher:latest
