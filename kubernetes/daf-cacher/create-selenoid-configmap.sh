#!/bin/bash

kubectl get cm selenoid 2>/dev/null && \
  kubectl delete configmap selenoid

kubectl create configmap selenoid --from-file=selenoid/config/

