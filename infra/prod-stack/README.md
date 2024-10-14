# How to setup the production stack

## Setup EC2 instance

```bash
ssh ec2-user@18.199.165.133
mkdir infra
```

## Copy docker compose file

```bash
scp -r infra/prod-stack ec2-user@18.199.165.133:/home/ec2-user/infra/prod-stack
```

## Setup docker

https://medium.com/@srijaanaparthy/step-by-step-guide-to-install-docker-on-amazon-linux-machine-in-aws-a690bf44b5fe

```bash
sudo yum update -y # update the package list
sudo yum install -y docker # install docker
sudo systemctl start docker # start docker daemon
sudo systemctl enable docker # enable docker to start on boot
sudo usermod -a -G docker $(whoami) # add the current user to the docker group
newgrp docker # activate the changes to groups
docker login # login to docker
```

## Setup docker-compose

```bash
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose # docker-compose (latest version)
sudo chmod +x /usr/local/bin/docker-compose # fix permissions
```

## Start the stack

```bash
docker-compose -f prod-stack.docker-compose.yaml up
```

## Delete the stack

```bash
docker-compose -f prod-stack.docker-compose.yaml down
```