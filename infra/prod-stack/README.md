# Production Stack

## Useful commands


- Validate cloud formation
    ```shell
    aws cloudformation validate-template \
      --template-body file://./infra.yaml
    ```

- Deploy Stack
    ```shell
    cd ../.. # must be urn from the root
    ./infra/prod-stack/scripts/deploy.sh
    ```
  
- Describe Stack status
    ```shell
    aws cloudformation describe-stacks \
        --stack-name noql-demo \
        --region eu-north-1 \
        --query 'Stacks[0].StackStatus'

- Get IP output value
    ```shell
    aws cloudformation describe-stacks \
      --stack-name noql-demo \
      --region eu-north-1 \
      --query "Stacks[0].Outputs[?OutputKey=='PublicIP'].OutputValue" \
      --output text
    ```

- Tear down stack
    ```shell
    aws cloudformation delete-stack \
        --stack-name noql-demo \
        --region eu-north-1
    ```
  
- Describe EC2 key-pairs
    ```shell
    aws ec2 describe-key-pairs \
        --region eu-north-1 \
        --output table
    ```
