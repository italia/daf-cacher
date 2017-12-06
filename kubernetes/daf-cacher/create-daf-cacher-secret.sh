#!/bin/bash

kubectl get secret daf-cacher-config 2>/dev/null && \
  kubectl delete secret daf-cacher-config

kubectl create secret generic daf-cacher-config --from-file=config.properties

