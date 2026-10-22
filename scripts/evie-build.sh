#!/usr/bin/env bash
# Run a Gradle task inside the evie-build Docker image.
# Usage: ./scripts/evie-build.sh [gradle args...]
set -euo pipefail

IMAGE=evie-build
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Persistent Gradle caches so deps aren't re-downloaded every build.
mkdir -p "${PROJECT_DIR}/.docker-gradle"
# Persist $HOME/.android inside HOME so the debug keystore survives per-run
# containers; otherwise every rebuild re-signs and reinstalls must uninstall.
mkdir -p "${PROJECT_DIR}/.docker-home"

docker run --rm \
  -u "$(id -u):$(id -g)" \
  -e HOME=/tmp/evie \
  -e GRADLE_USER_HOME=/tmp/evie/gradle \
  -v "${PROJECT_DIR}:/home/gradle/project" \
  -v "${PROJECT_DIR}/.docker-gradle:/tmp/evie/gradle" \
  -v "${PROJECT_DIR}/.docker-home:/tmp/evie/.android" \
  -w /home/gradle/project \
  "${IMAGE}" \
  gradle "${@}"