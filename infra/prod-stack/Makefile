TEMPLATE ?= infra.yaml
STACK_NAME ?= noql-demo
REGION ?= eu-central-1

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
		--template-body file://$(TEMPLATE)


# Deploy stack
deploy: check-ssh-key
	aws cloudformation create-stack \
		--stack-name $(STACK_NAME) \
		--template-body file://$(TEMPLATE) \
		--parameters ParameterKey=KeyName,ParameterValue=$(SSH_KEY_NAME) \
		--capabilities CAPABILITY_NAMED_IAM \
		--region $(REGION)

# Describe stack status
status:
	aws cloudformation describe-stacks \
		--stack-name $(STACK_NAME) \
		--region $(REGION) \
		--query 'Stacks[0].StackStatus'

# Get IP output value (public IP of the deployed EC2 instance)
get-public-ip:
	aws cloudformation describe-stacks \
		--stack-name $(STACK_NAME) \
		--region $(REGION) \
		--query "Stacks[0].Outputs[?OutputKey=='PublicIP'].OutputValue" \
		--output text

# Show recent CloudFormation events (useful for errors)
events:
	aws cloudformation describe-stack-events \
		--stack-name $(STACK_NAME) \
		--region $(REGION) \
		--query 'StackEvents[?ResourceStatusReason!=`null`].[LogicalResourceId,ResourceType,ResourceStatus,ResourceStatusReason]' \
		--output table

# Tear down stack
tear-down:
	aws cloudformation delete-stack \
		--stack-name $(STACK_NAME) \
		--region $(REGION)

# Output true or false based on whether or not the stack exists
stack-exists:
	@aws cloudformation describe-stacks \
		--stack-name $(STACK_NAME) \
		--region $(REGION) >/dev/null 2>&1 && \
		echo true || echo false

# Describes EC2 key-pairs
ec2-key-pairs:
	aws ec2 describe-key-pairs \
		--region $(REGION) \
		--query "KeyPairs[*].KeyName" \
		--output table

# Check SSH key is defined
check-ssh-key:
ifndef SSH_KEY_NAME
	$(error SSH_KEY_NAME is required for deploy)
endif