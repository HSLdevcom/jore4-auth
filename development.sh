#!/usr/bin/env bash

set -eo pipefail

# allow running from any working directory
WD=$(dirname "$0")
cd "${WD}"

# initialize package folder
mkdir -p ./docker

DOCKER_COMPOSE_CMD="docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose.custom.yml"

function check_docker {
  curl https://raw.githubusercontent.com/HSLdevcom/jore4-tools/main/docker/download-docker-bundle.sh | bash
}

function start {
  check_docker
  $DOCKER_COMPOSE_CMD up --build -d jore4-auth jore4-testdb
}

function stop_all {
  check_docker
  $DOCKER_COMPOSE_CMD stop
}

function remove_all {
  check_docker
  $DOCKER_COMPOSE_CMD down
}

function build {
  mvn install
}

function run_tests {
  mvn test
}

function usage {
  echo "
  Usage $(basename $0) <command>

  build
    Build the project locally

  start
    Start auth service and testdb in Docker container

  stop
    Stop auth Docker containers

  remove
    Stop and remove auth Docker containers

  test
    Run tests locally

  help
    Show this usage information
  "
}

if [[ -z ${1} ]]; then
  usage
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
    usage
    ;;
  esac
fi
