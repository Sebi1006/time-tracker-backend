#!/usr/bin/env bash

kubectl delete -f eks-ingress.yaml

kubectl delete -f eks-alb-ingress-controller.yaml

kubectl delete -f rbac-role-alb-ingress-controller.yaml

kubectl delete -f eks-service.yaml

kubectl delete -f eks-deployment.yaml

aws cloudformation delete-stack --stack-name eksctl-time-tracker-backend-cluster-nodegroup-EKS-private-workers

aws cloudformation delete-stack --stack-name eksctl-time-tracker-backend-cluster-nodegroup-EKS-public-workers

aws cloudformation delete-stack --stack-name eksctl-time-tracker-backend-cluster-cluster

eksctl delete cluster -f cluster.yaml
