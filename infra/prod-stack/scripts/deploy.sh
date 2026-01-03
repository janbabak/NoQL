#!/bin/bash

set -eo pipefail

# This script deploys infrastructure defined in the prod-stack/infra.yaml file

STACK_NAME="noql-demo"
REGION="eu-north-1"
INFRA_FILE="./infra/prod-stack/infra.yaml"
SSH_KEY_NAME="macbookPro14"

aws cloudformation create-stack \
  --stack-name ${STACK_NAME} \
  --template-body "file://${INFRA_FILE}" \
  --parameters "ParameterKey=KeyName,ParameterValue=${SSH_KEY_NAME}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --region ${REGION}