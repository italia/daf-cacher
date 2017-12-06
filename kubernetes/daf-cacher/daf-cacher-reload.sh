#!/bin/bash

deployments=( "daf-cacher-fe-deployment" "daf-cacher-seeder-deployment" "daf-cacher-worker-deployment" )
for i in "${deployments[@]}"
do
  kubectl patch deployment "${i}" -p \
    "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"date\":\"`date +'%s'`\"}}}}}"
done
