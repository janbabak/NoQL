#!/bin/bash
set -eo pipefail

# Check if pull reqeust version is greater than main version

# $1 = PR version
# $2 = main version
PR_VERSION=$1
MAIN_VERSION=$2

version_regex='^([0-9]+)\.([0-9]+)\.([0-9]+)$'

if ! [[ $PR_VERSION =~ $version_regex ]]; then
    echo "PR version '$PR_VERSION' is not a valid semantic version"
    exit 1
fi

if ! [[ $MAIN_VERSION =~ $version_regex ]]; then
    echo "Main version '$MAIN_VERSION' is not a valid semantic version"
    exit 1
fi

IFS='.' read -r -a pr_parts <<< "$PR_VERSION"
IFS='.' read -r -a main_parts <<< "$MAIN_VERSION"

pr_major=${pr_parts[0]}
pr_minor=${pr_parts[1]}
pr_patch=${pr_parts[2]}

main_major=${main_parts[0]}
main_minor=${main_parts[1]}
main_patch=${main_parts[2]}

# Check if PR version is greater than main version
if (( pr_major < main_major )); then
    echo "Major version must be incremented"
    exit 1
elif (( pr_major == main_major )); then
    if (( pr_minor < main_minor )); then
        echo "Minor version must be incremented"
        exit 1
    elif (( pr_minor == main_minor )); then
        if (( pr_patch <= main_patch )); then
            echo "Patch version must be incremented"
            exit 1
        fi
    fi
fi

echo "Version increment is valid"