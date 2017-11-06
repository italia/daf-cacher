#!/usr/bin/env bash

set -x

if [[ ! -f /etc/config.properties ]]; then
    echo "/etc/config.properties file not found"
    exit 1
fi

cp /etc/config.properties /usr/src/daf-metabase-cacher/

export JAVA_OPTS="-Xms1024m -Xmx1024m"
export JAVA_CLASS_PATH="/usr/src/daf-metabase-cacher/*:/usr/src/daf-metabase-cacher/dependency/*"

exec /usr/bin/daf-metabase-cacher "$@"
