#!/bin/bash
set -e

#
# the big CloudFoundry installer
#

CLOUD_DOMAIN=${DOMAIN:-run.pivotal.io}
CLOUD_TARGET=api.${DOMAIN}

function login(){
    cf api | grep ${CLOUD_TARGET} || cf api ${CLOUD_TARGET} --skip-ssl-validation
    cf a | grep OK || cf login
}

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}

## COMMON

function deploy_app(){
    APP_NAME=$1
    echo "APP_NAME=$APP_NAME"
    cd $APP_NAME
    cf push $APP_NAME
    cd ..
}

function deploy_service(){
    N=$1
    D=`app_domain $N`
    JSON='{"uri":"http://'$D'"}'
    echo cf cups $N  -p $JSON
    cf cups $N -p $JSON
}

function deploy_config_service(){
    NAME=config-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_eureka_service(){
    NAME=eureka-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_hystrix_dashboard(){
    deploy_app hystrix-dashboard
}


function deploy_reservation_service(){
    cf cs elephantsql turtle reservations-postgresql
    deploy_app reservation-service
}

function deploy_reservation_client(){
    deploy_app reservation-client
}

function reset(){

    echo "reset.."
    apps="hystrix-dashboard reservation-client reservation-service eureka-service config-service"
    apps_arr=( $apps )
    for a in "${apps_arr[@]}";
    do
         echo $a
         cf d -f $a
    done

    services="reservations-postgresql eureka-service config-service"
    services_arr=( $services )
    for s in "${services_arr[@]}";
    do
        echo $s
        cf ds -f $s
    done

    cf delete-orphaned-routes -f

}

###
### INSTALLATION STEPS
###

#mvn -DskipTests=true clean install

function install(){
  apps="hystrix-dashboard reservation-client reservation-service eureka-service config-service"
  apps_arr=( $apps )
  for a in "${apps_arr[@]}";
  do
    echo $a
    mvn  -f $a/pom.xml -DskipTests=true clean install
  done
}

#login
reset
install
deploy_config_service
deploy_eureka_service
#deploy_hystrix_dashboard
deploy_reservation_service
deploy_reservation_client
