# Production Stack

## Useful commands

- [Makefile](./Makefile) provides abstraction for commonly used commands
- Environment variables
  - `TEMPLATE` - specifies cloudformation file with infrastructure, default [infra.yaml](infra.yaml)
  - `STACK_NAME` - stack name, default `noql-demo`
  - `REGION` - AWS region, default `eu-central-1` (Stockholm)
  - `SSH_KEY_NAME` - required for `make deploy`, define name of the SSH key used to access the EC2 instance.
    You can list your keys using `make ec2-key-pairs`


- Validate cloud formation from `TEMPLATE`
    ```shell
    make validate
    ```

- Deploy Stack
    ```shell
    make deploy
    ```
  
- Describe Stack status
    ```shell
    make status
    ```

- Get IP output value (public IP of the deployed EC2 instance)
    ```shell
    make get-public-ip
    ```

- Show recent CloudFormation events (useful for errors)
    ```shell
    make events
    ```

- Tear down stack
    ```shell
    make tear-down
    ```

- Output true or false based on whether or not the stack exists
    ```shell
    make stack-exists
    ```
  
- Describe EC2 key-pairs (useful for defining `SSH_KEY_NAME`) env
    ```shell
    make ec2-key-pairs 
    ```
