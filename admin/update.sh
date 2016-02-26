#!/bin/bash

# Updates the codebase and puts the files in the right place

BUILD_DIR=~/build
DIST_DIR=~/dist/v1

if [ ! -e $DIST_DIR/lib ]; then
    mkdir -p $DIST_DIR/lib
fi

# Build
cd $BUILD_DIR/pregnaware/progresssvc
sbt assembly
JAR_FILE=`ls target/scala-2.11/*.jar`
cp $JAR_FILE $DIST_DIR/lib/
echo "Copied new JAR file: $JAR_FILE"

# Update web files
cd $DIST_DIR
cp -r $BUILD_DIR/pregnaware/frontend/www $DIST_DIR/
cd $DIST_DIR/www/js
npm install
echo "Updated website"

echo
echo "######################"
echo "# Pregnaware updated #"
echo "######################"
echo