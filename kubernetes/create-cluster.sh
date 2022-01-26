#!/usr/bin/env bash

echo "Step 1: Create Cluster"
eksctl create cluster -f cluster.yaml

echo "Step 2a: Backend Deployment"
kubectl apply -f eks-deployment.yaml

echo "Step 3a: Backend Service"
kubectl apply -f eks-service.yaml

echo "Step 2b: Microservice Deployment"
kubectl apply -f eks-deployment-ms.yaml

echo "Step 3b: Microservice Service"
kubectl apply -f eks-service-ms.yaml

GROUP_ID="$(aws ec2 describe-security-groups --filters Name=vpc-id,Values='vpc-0f6daeb4ad1c716ea' Name=group-name,Values='eksctl-time-tracker-backend-cluster-nodegroup-EKS-public-workers-SG-*' --query 'SecurityGroups[*].{GroupName:GroupName,GroupId:GroupId}' | jq '.[].GroupId')"
GROUP_ID=${GROUP_ID#'"'}
GROUP_ID=${GROUP_ID%'"'}

aws ec2 authorize-security-group-ingress --group-id $GROUP_ID --protocol tcp --port 31479 --cidr 0.0.0.0/0

aws iam create-policy --policy-name ALBIngressControllerIAMPolicy --policy-document file://iam-policy.json

echo "Step 4: RBAC Role"
kubectl apply -f rbac-role-alb-ingress-controller.yaml

eksctl utils associate-iam-oidc-provider --region eu-central-1 --cluster time-tracker-backend-cluster --approve

aws iam create-role --role-name eks-alb-ingress-controller --assume-role-policy-document file://eks-ingress-trust-iam-policy.json

aws iam attach-role-policy --role-name eks-alb-ingress-controller --policy-arn=arn:aws:iam::179849223048:policy/ALBIngressControllerIAMPolicy

echo "Step 5: Annotate Service Account"
kubectl annotate serviceaccount -n kube-system alb-ingress-controller eks.amazonaws.com/role-arn=arn:aws:iam::179849223048:role/eks-alb-ingress-controller --overwrite

echo "Step 6: Alb Ingress Controller"
kubectl apply -f eks-alb-ingress-controller.yaml

echo "Step 7: Ingress"
kubectl apply -f eks-ingress.yaml
