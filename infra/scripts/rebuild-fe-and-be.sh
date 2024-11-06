#! /bin/bash

# rebuild frontend and backend docker images (bump up their versions before running this script)

./infra/dockerImages/frontend/frontend.build.sh
./infra/dockerImages/backend/backend.build.sh