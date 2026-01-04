NOQL_TEMPLATE ?= infra.yaml
NOQL_STACK_NAME ?= noql-demo
NOQL_AWS_REGION ?= eu-north-1

.PHONY: \
	validate \
	deploy \
	check-ssh-key \
	status \
	get-public-ip \
	events \
	tear-down \
	stack-exists \
	ec2-key-pairs

# Validate cloud formation template
validate:
	aws cloudformation validate-template \
		--template-body file://$(NOQL_TEMPLATE) \
		--region $(NOQL_AWS_REGION)


# Deploy stack
deploy: check-ssh-key
	aws cloudformation deploy \
		--stack-name $(NOQL_STACK_NAME) \
		--template-file $(NOQL_TEMPLATE) \
		--parameter-overrides KeyName=$(NOQL_SSH_KEY_NAME) \
		--capabilities CAPABILITY_NAMED_IAM \
		--region $(NOQL_AWS_REGION)

# Wait until stack deployment is finished
deploy-wait:
	aws cloudformation wait stack-create-complete \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION)

# Describe stack status
status:
	aws cloudformation describe-stacks \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION) \
		--query 'Stacks[0].StackStatus'

# Get IP output value (public IP of the deployed EC2 instance)
public-ip:
	@aws cloudformation describe-stacks \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION) \
		--query "Stacks[0].Outputs[?OutputKey=='PublicIP'].OutputValue" \
		--output text

# Show recent CloudFormation events (useful for errors)
events:
	aws cloudformation describe-stack-events \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION) \
		--query 'StackEvents[?ResourceStatusReason!=`null`].[LogicalResourceId,ResourceType,ResourceStatus,ResourceStatusReason]' \
		--output table

# Tear down stack
tear-down:
	aws cloudformation delete-stack \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION)

# Output true or false based on whether or not the stack exists
stack-exists:
	@aws cloudformation describe-stacks \
		--stack-name $(NOQL_STACK_NAME) \
		--region $(NOQL_AWS_REGION) >/dev/null 2>&1 && \
		echo true || echo false

# Describes EC2 key-pairs
ec2-key-pairs:
	aws ec2 describe-key-pairs \
		--region $(NOQL_AWS_REGION) \
		--query "KeyPairs[*].KeyName" \
		--output table

# Check SSH key is defined
check-ssh-key:
ifndef NOQL_SSH_KEY_NAME
	$(error NOQL_SSH_KEY_NAME is required for deploy)
endif