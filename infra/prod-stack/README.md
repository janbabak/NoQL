# Production Stack

## Useful commands


- Validate cloud formation
    ```bash
    aws cloudformation validate-template \
      --template-body file://./infra.yaml
    ```

- Deploy Stack
    ```bash
    cd ../.. # must be urn from the root
    ./infra/prod-stack/scripts/deploy.s
    ```
  
- Describe EC2 key-pairs
    ```bash
    aws ec2 describe-key-pairs \                                                                               252 ✘  18:10:41  
        --region eu-north-1 \
        --output table
    ```
