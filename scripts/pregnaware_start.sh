#!/bin/bash

ROOT_DIR=~/dist
java -jar $ROOT_DIR/latest/lib/Pregnaware-assembly-1.0.jar > $ROOT_DIR/log/pregnaware.log 2>&1 &
