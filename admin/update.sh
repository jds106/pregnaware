#!/bin/bash

# Updates the codebase and puts the files in the right place

BUILD_DIR=~/build/pregnaware
DIST_DIR=~/dist
DIST_DIR_VERSION=$DIST_DIR/v1

if [ ! -e $DIST_DIR_VERSION/lib ]; then
    mkdir -p $DIST_DIR_VERSION/lib
fi

if [ ! -e $DIST_DIR_VERSION/scripts ]; then
    mkdir -p $DIST_DIR_VERSION/scripts
fi

echo "Stopping MYSQL server..."
sudo service mysql stop
echo "MYSQL Server stopped"

# Build
cd $BUILD_DIR/service
sbt assembly
JAR_FILE=`ls target/scala-2.11/*.jar`
cp $JAR_FILE $DIST_DIR_VERSION/lib/
echo "Copied new JAR file: $JAR_FILE"

# Update web files
cd $DIST_DIR_VERSION
cp -r $BUILD_DIR/scripts/* $DIST_DIR_VERSION/scripts/
echo "Scripts updated"

# Update the script files
cp -r $BUILD_DIR/frontend/www $DIST_DIR_VERSION/

# Update the Consul config
if [ ! -e $DIST_DIR/etc/consul ]; then
    mkdir -p $DIST_DIR/etc/consul
fi

cp -r $BUILD_DIR/etc/consul $DIST_DIR/etc/

echo "Restarting MYSQL server..."
sudo service mysql start
echo "MYSQL Server started"

echo
echo "######################"
echo "# Pregnaware updated #"
echo "######################"
echo
