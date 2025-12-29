#!/bin/bash
set -e

# This script updates coverage `BADGE_FILE` JSON in `BRANCH` branch which is used by badge in README.md
# Usage: ./update-coverage-badge.sh <badge_file> <total_coverage>
# GITHUB_TOKEN environment variable is required for push

# Variables
BADGE_FILE="${1:-backend_coverage.json}"
TOTAL_COVERAGE="$2"
BRANCH="coverage-badge"

# Generate JSON
jq -n \
  --arg label "Backend Coverage" \
  --arg message "$TOTAL_COVERAGE%" \
  --arg color "brightgreen" \
  '{schemaVersion:1, label:$label, message:$message, color:$color}' \
  > "$BADGE_FILE"

# Git commit & push using GITHUB_TOKEN
git config user.name "github-actions"
git config user.email "github-actions@github.com"
git fetch origin "$BRANCH" || true
git checkout -B "$BRANCH"
git add "$BADGE_FILE"
git commit -m "Update coverage badge [skip ci]" || exit 0
git push --force https://x-access-token:${GH_ACCESS_TOKEN}@github.com/janbabak/NoQL.git "$BRANCH"