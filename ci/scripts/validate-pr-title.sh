#!/bin/bash
set -e

# Validates Pull reqeust title format

if [ -z "$PR_TITLE" ]; then
  echo "❌ PR_TITLE environment variable is not set"
  exit 1
fi

echo "PR title: $PR_TITLE"

if ! [[ "$PR_TITLE" =~ ^[0-9]+ .+ ]]; then
  echo "❌ PR title must start with an issue number, a space, and a description (e.g. '123 Fix bug')"
  exit 1
fi