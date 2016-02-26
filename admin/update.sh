#!/bin/bash

# Updates the codebase and puts the files in the right place

BUILD_DIR=~/build
DIST_DIR=~/dist

# Build
cd ~/build/pregnaware/progresssvc
sbt assembly

cd ~/dist
cp -r ~/build/pregnaware/frontend/www ~/dist/
cd 
