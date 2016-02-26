#!/bin/bash

# Updates the codebase and puts the files in the right place

BUILD_DIR=~/build
DIST_DIR=~/dist

# Build
cd $DIST_DIR/pregnaware/progresssvc
sbt assembly
JAR_FILE=`ls target/scala-2.11/*.jar`
cp $JAR_FILE $DIST_DIR/bin/
echo "Copied new JAR file: $JAR_FILE"

# Update web files
cd ~/dist
cp -r ~/build/pregnaware/frontend/www ~/dist/
cd ~/dist/www/js
npm install
echo "Updated website"

echo
echo "######################"
echo "# Pregnaware updated #"
echo "######################"
echo