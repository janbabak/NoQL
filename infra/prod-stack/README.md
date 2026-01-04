# Deployment guide

## Useful commands

- [Makefile](./Makefile) provides abstraction for commonly used commands
- Environment variables
  - `NOQL_TEMPLATE` - specifies cloudformation file with infrastructure, default [infra.yaml](infra.yaml)
  - `NOQL_STACK_NAME` - stack name, default `noql-demo`
  - `NOQL_AWS_REGION` - AWS region, default `eu-north-1` (Stockholm)
  - `NOQL_SSH_KEY_NAME` - required for `make deploy`; define the name of the SSH key used to access the EC2 instance.
    Note that SSH key pairs are region-specific in AWS. You can list your keys in the configured region using `make ec2-key-pairs`.


- **Validate CloudFormation template** from `NOQL_TEMPLATE`
    ```shell
    make validate
    ```

- **Deploy Stack**
    ```shell
    make deploy # if NOQL_SSH_KEY_NAME env is defined or
    make deploy NOQL_SSH_KEY_NAME=macbookPro14
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
    make get-public-ip
    ```

- **Show recent CloudFormation events** (useful for errors)
    ```shell
    make events
    ```

- **Example full deploy workflow:**
    ```shell
    export NOQL_SSH_KEY_NAME=macbookPro14
    export NOQL_AWS_REGION=eu-north-1
    make validate
    make deploy
    make status
    make get-public-ip
    ```

- **Tear down stack**
    ```shell
    make tear-down
    ```

- Output true or false based on whether the **stack exists**
    ```shell
    make stack-exists
    ```
  
- **Describe EC2 key-pairs** (useful for defining `NOQL_SSH_KEY_NAME` environment variable)
    ```shell
    make ec2-key-pairs 
    ```

- **Follow Docker Compose logs** on the EC2 instance (SSH into the instance first)
    ```shell
    ssh -i /path/to/your/key.pem ec2-user@$(make get-public-ip)
    docker-compose logs -f
    ```