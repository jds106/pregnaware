#/bin/bash

CONSUL_BIN=~/dist/bin/consul
ROOT_DIR=~/dist

ARGS="-bootstrap -server -dev"
ARGS="$ARGS -config-dir=$ROOT_DIR/etc/consul"
ARGS="$ARGS -data-dir=$ROOT_DIR/data/consul"
ARGS="$ARGS -ui -ui-dir=$ROOT_DIR/bin/consului"

nohup $CONSUL_BIN agent $ARGS  > $ROOT_DIR/log/consul.log 2>&1 &