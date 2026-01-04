# Deployment guide

## Useful commands

- [Makefile](./Makefile) provides abstraction for commonly used commands
- Environment variables
  - `NOQL_TEMPLATE` - specifies cloudformation file with infrastructure, default [infra.yaml](infra.yaml)
  - `NOQL_STACK_NAME` - stack name, default `noql-demo`
  - `NOQL_AWS_REGION` - AWS region, default `eu-north-1` (Stockholm)
  - `NOQL_SSH_KEY_NAME` - required for `make deploy`, define name of the SSH key used to access the EC2 instance.
    You can list your keys using `make ec2-key-pairs`


- **Validate cloud formation** from `NOQL_TEMPLATE`
    ```shell
    make validate
    ```

- **Deploy Stack**
    ```shell
    make deploy
    ```
  
- **Describe Stack status**
    ```shell
    make status
    ```
  | Status                  | Meaning                                                         |
  |-------------------------|-----------------------------------------------------------------|
  | CREATE_FAILED           | Something went wrong during creation; no resources remain.      |
  | ROLLBACK_COMPLETE       | Stack creation failed and CloudFormation cleaned up resources   |
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

- **Tear down stack**
    ```shell
    make tear-down
    ```

- Output true or false based on whether the **stack exists**
    ```shell
    make stack-exists
    ```
  
- **Describe EC2 key-pairs** (useful for defining `SSH_KEY_NAME`) env
    ```shell
    make ec2-key-pairs 
    ```
