# Deployment guide

This document describes how to deploy the **NoQL** application to AWS, either automatically using **GitHub Actions** or
manually from your local machine using **Make** and the **AWS CLI**.

---

## Table of Contents

- [Deployment guide](#deployment-guide)
- [Deploy stack using GitHub Actions](#deploy-stack-using-github-actions)
- [Deploy stack locally](#deploy-stack-locally)
    - [Software Requirements](#software-requirements)
    - [Configuration](#configuration)
    - [Commands](#commands)

---

## Deploy stack using GitHub actions

[▶️ Deploy Stack](https://github.com/janbabak/NoQL/actions/workflows/stack-deploy.yaml)

[▶️ Tear-down Stack](https://github.com/janbabak/NoQL/actions/workflows/stack-tear-down.yaml)

---

## Deploy stack locally

[Makefile](./Makefile) provides abstraction for commonly used commands

### Software Requirements

- [Make](https://cs.wikipedia.org/wiki/Make)
- [AWS CLI](https://aws.amazon.com/cli/)
- [Bash](https://cs.wikipedia.org/wiki/Bash)

### Configuration

- **Set up AWS CLI**

```shell
aws configure
```

- **Define environment variables**
    - `NOQL_TEMPLATE` - specifies cloudformation file with infrastructure, default [infra.yaml](infra.yaml)
    - `NOQL_STACK_NAME` - stack name, default `noql-demo`
    - `NOQL_AWS_REGION` - AWS region, default `eu-north-1` (Stockholm)
    - `NOQL_SSH_KEY_NAME` - required for `make deploy`; define the name of the SSH key used to access the EC2 instance.
      Note that SSH key pairs are region-specific in AWS. You can list your keys in the configured region using
      `make ec2-key-pairs`.

### Commands

- **Validate CloudFormation template** from `NOQL_TEMPLATE`
    ```shell
    make validate
    ```

- **Describe EC2 key-pairs** (useful for defining `NOQL_SSH_KEY_NAME` environment variable)
    ```shell
    make ec2-key-pairs 
    ```

- **Add correct permissions to SSH key**, if newly created
    ```shell
    chmod 400 ~/Developer/privateCredentials/awsNoqlMacbookPro14.pem
    ```

- **Deploy Stack**
    ```shell
    make deploy # if NOQL_SSH_KEY_NAME env is defined or
    make deploy NOQL_SSH_KEY_NAME=macbookPro14 # change ssh key
    ```

- **Describe Stack status**
    ```shell
    make status
    ```
  | Status                  | Meaning                                                         |
    |-------------------------|-----------------------------------------------------------------|
  | CREATE_FAILED           | Something went wrong during creation; no resources remain.      |
  | ROLLBACK_COMPLETE       | Stack creation failed and CloudFormation cleaned up resources   |
  | ROLLBACK_IN_PROGRESS    | Stack creation failed and rollback is currently in progress     |
  | CREATE_COMPLETE         | Stack creation succeeded and resources are ready                |
  | UPDATE_COMPLETE         | Stack update succeeded                                          |
  | UPDATE_ROLLBACK_COMPLETE| Update failed and CloudFormation reverted to the previous state |

- **Get IP output value** (public IP of the deployed EC2 instance)
    ```shell
    make public-ip
    ```

- **Show recent CloudFormation events** (useful for errors)
    ```shell
    make events
    ```

- **Tear down stack**
    ```shell
    make tear-down
    ```

- Output true or false based on whether the **stack exists**
    ```shell
    make stack-exists
    ```

- **Follow Docker Compose logs** on the EC2 instance (SSH into the instance first)
    ```shell
    ssh -i /path/to/your/key.pem ec2-user@$(make public-ip)
    docker-compose logs -f # on host machine
    ```

- **Example full deploy workflow:**
    ```shell
    # define environment variables
    export NOQL_SSH_KEY_NAME=macbookPro14
    export NOQL_AWS_REGION=eu-north-1
  
    # validate cloud formation
    make validate
  
    # deploy AWS stack
    make deploy
  
    # verify status is `CREATE_COMPLETE`
    make status
  
    # get public IP address of EC2 instance
    export NOQL_PROD_STACK_IP=$(make public-ip)
    echo $NOQL_PROD_STACK_IP
  
    # change backend url env in /infra/local-stack/.env.frontend-prod
    
    # copy files
    scp -i  ~/Developer/privateCredentials/awsNoqlMacbookPro14.pem  \
      ../local-stack/prod-stack.docker-compose.yaml \
      ../local-stack/.env.backend-prod \
      ../local-stack/.env.frontend-prod \
      ec2-user@${NOQL_PROD_STACK_IP}:~/noql-app/
  
    # Start docker compose on the remove machin
    ssh -i ~/Developer/privateCredentials/awsNoqlMacbookPro14.pem ec2-user@${NOQL_PROD_STACK_IP} << 'EOF'
    docker compose \
      --file noql-app/prod-stack.docker-compose.yaml \
      --env-file noql-app/.env.backend-prod \
      --project-name prod-stack \
      up -d
    EOF
    ```