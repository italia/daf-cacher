#!/usr/bin/env bash

mvn clean dependency:copy-dependencies  package -Dmaven.test.skip=true && \
docker build -t italia/daf-metabase-cacher .
