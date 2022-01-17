#!/usr/bin/env bash

kubectl delete -f eks-ingress.yaml

kubectl delete -f eks-alb-ingress-controller.yaml

kubectl delete -f rbac-role-alb-ingress-controller.yaml

kubectl delete -f eks-service.yaml

kubectl delete -f eks-deployment.yaml

eksctl delete cluster -f cluster.yaml
