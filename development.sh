#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

DOCKER_COMPOSE_CMD="docker compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function download_docker_compose_bundle {
  curl https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/download-docker-bundle.sh | bash
}

function start {
  download_docker_compose_bundle
  $DOCKER_COMPOSE_CMD up --build -d jore4-auth jore4-testdb
}

function stop_all {
  download_docker_compose_bundle
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  download_docker_compose_bundle
  $DOCKER_COMPOSE_CMD down
}

function build {
  mvn install
}

function run_tests {
  mvn test
}

function print_usage {
  echo "
  Usage: $(basename "$0") <command>

  build
    Build the project locally

  start
    Start Docker containers for authentication service and test database

  stop
    Stop Docker containers

  remove
    Stop and remove Docker containers

  test
    Run tests locally

  help
    Show this usage information
  "
}

if [[ -z ${1} ]]; then
  print_usage
else
  case $1 in
  start)
    start
    ;;

  stop)
    stop_all
    ;;

  remove)
    remove_all
    ;;

  help)
    usage
    ;;

  build)
    build
    ;;

  test)
    run_tests
    ;;

  *)
    print_usage
    ;;
  esac
fi
