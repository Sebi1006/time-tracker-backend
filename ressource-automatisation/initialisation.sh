#!/usr/bin/env bash

# Create tenant_master table in DynamoDB for tenant resources
aws dynamodb create-table --table-name tenant_master --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

# Create user pool for free subscription tenants
USER_POOL_ID="$(aws cognito-idp create-user-pool --pool-name time-tracker-free --policies 'PasswordPolicy={MinimumLength=8,RequireUppercase=true,RequireLowercase=true,RequireNumbers=true,RequireSymbols=true,TemporaryPasswordValidityDays=7}' --username-attributes email --mfa-configuration OFF --username-configuration CaseSensitive=false --admin-create-user-config AllowAdminCreateUserOnly=false --schema Name=email,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=true,'StringAttributeConstraints={MinLength=1,MaxLength=256}' Name=first_name,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=1,MaxLength=256}' Name=last_name,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=1,MaxLength=256}' Name=sub_model,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=1,MaxLength=256}' Name=entrance_date,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=1,MaxLength=256}' Name=phone,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=0,MaxLength=256}' Name=avatar_url,AttributeDataType=String,DeveloperOnlyAttribute=false,Mutable=true,Required=false,'StringAttributeConstraints={MinLength=0,MaxLength=256}' | jq '.UserPool .Id')"
USER_POOL_ID=${USER_POOL_ID#'"'}
USER_POOL_ID=${USER_POOL_ID%'"'}

# Create groups in user pool
aws cognito-idp create-group --group-name ROLE_USER --user-pool-id $USER_POOL_ID
aws cognito-idp create-group --group-name ROLE_ADMIN --user-pool-id $USER_POOL_ID

# Create app client in user pool
aws cognito-idp create-user-pool-client --user-pool-id $USER_POOL_ID --client-name app-client-free --generate-secret --refresh-token-validity 90 --access-token-validity 12 --id-token-validity 12 --explicit-auth-flows ALLOW_ADMIN_USER_PASSWORD_AUTH ALLOW_CUSTOM_AUTH ALLOW_USER_PASSWORD_AUTH ALLOW_USER_SRP_AUTH ALLOW_REFRESH_TOKEN_AUTH --prevent-user-existence-errors ENABLED | jq '.UserPoolClient' > UserPoolClient.json

APP_CLIENT_ID=$(jq '.ClientId' UserPoolClient.json)
APP_CLIENT_ID=${APP_CLIENT_ID#'"'}
APP_CLIENT_ID=${APP_CLIENT_ID%'"'}

APP_SECRET_ID=$(jq '.ClientSecret' UserPoolClient.json)
APP_SECRET_ID=${APP_SECRET_ID#'"'}
APP_SECRET_ID=${APP_SECRET_ID%'"'}

item=$(jo id=$(jo S=time-tracker-free) appSecretId=$(jo S=$APP_SECRET_ID) appClientId=$(jo S=$APP_CLIENT_ID) userPoolId=$(jo S=$USER_POOL_ID) organizationName=$(jo S=Free Version) | jq '.' > item.json)

aws dynamodb put-item --table-name tenant_master --item file://item.json

APPLICATION_ID="$(aws amplify create-app --name time-tracker-free-frontend --repository https://gitlab.com/t5172/frontend --access-token glpat-aLT1jKyTeLTJNh3deCpG --iam-service-role-arn arn:aws:iam::179849223048:role/amplifyconsole-backend-role --build-spec amplify.yml --environment-variables NEXT_PUBLIC_TENANT=time-tracker-free  --enable-branch-auto-build --no-enable-auto-branch-creation | jq '.app .appId')"
APPLICATION_ID=${APPLICATION_ID#'"'}
APPLICATION_ID=${APPLICATION_ID%'"'}
aws amplify create-branch --branch-name main --app-id $APPLICATION_ID

aws amplify start-job --branch-name main --job-type RELEASE --app-id $APPLICATION_ID

