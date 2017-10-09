#!/usr/bin/env bash

set -x

MAIN_CLASS=""

case "$1" in
        server)
            MAIN_CLASS=com.github.italia.daf.Server
            ;;

        seeder)
            MAIN_CLASS=com.github.italia.daf.Seeder
            ;;

        worker)
            MAIN_CLASS=com.github.italia.daf.CacheWorker
            ;;
        *)
            echo $"Usage: $0 {server|seeder|worker}"
            exit 1
esac

exec java $JAVA_OPTS -cp $JAVA_CLASS_PATH $MAIN_CLASS config.properties


