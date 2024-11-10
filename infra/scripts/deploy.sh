#! /bin/bash

# copies files to a production server and opens an ssh connection

scp -r infra/prod-stack ${PROD_STACK_SSH}:/home/ec2-user/infra
ssh $PROD_STACK_SSH
