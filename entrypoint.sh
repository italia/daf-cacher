#!/usr/bin/env bash

set -x
DAF_CACHER_CONFIG_FILE=${DAF_CACHER_CONFIG_FILE:-/etc/config.properties}
if [[ ! -f $DAF_CACHER_CONFIG_FILE ]]; then
    echo "${DAF_CACHER_CONFIG_FILE} file not found"
    exit 1
fi

cp $DAF_CACHER_CONFIG_FILE /usr/src/daf-metabase-cacher/config.properties

export JAVA_OPTS="-Xms1024m -Xmx1024m"
export JAVA_CLASS_PATH="/usr/src/daf-metabase-cacher/*:/usr/src/daf-metabase-cacher/dependency/*"

exec /usr/bin/daf-metabase-cacher "$@"
