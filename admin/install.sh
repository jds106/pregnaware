#!/bin/bash

# Installs all required software for a new server

ROOT=/home/ubuntu/dist
echo Using root: $ROOT

if [ ! -n "$ROOT" ]; then
    echo Creating root directory: $ROOT
    mkdir -p $ROOT
    mkdir -p $ROOT/bin
fi

# Install SBT
if [ $(command -v sbt) ]; then 
    echo "SBT installed"; 
else 
    # Installation taken from http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html
    echo "##################"
    echo "# Installing SBT #"
    echo "##################"
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
    sudo apt-get update
    sudo apt-get install sbt
fi

# Install NGINX
if [ $(command -v nginx) ]; then 
    echo "NGINX installed"; 
else 
    echo "####################"
    echo "# Installing NGINX #"
    echo "####################"
    sudo apt-get install nginx
    ./update_nginx.sh
fi

# Install Consul
if [ -e $ROOT/bin/consul ]; then 
    echo "Consul installed"; 
else 
    echo "#####################"
    echo "# Installing Consul #"
    echo "#####################"

    # Taken from https://www.consul.io/downloads.html
    TMP_DIR='date +%Y%m%d%H%M%S'
    mkdir $TMP_DIR
    (
        cd $TMP_DIR
        wget https://releases.hashicorp.com/consul/0.6.3/consul_0.6.3_linux_amd64.zip
        ZIP_FILE=`ls -1 *.zip`
        unzip $ZIP_FILE
        rm $ZIP_FILE
        mv consul $ROOT/bin/
    )
    rm -rf $TMP_DIR
fi

# Install Consul GUI
if [ -e $ROOT/bin/consului ]; then 
    echo "Consul UI installed"; 
else 
    echo "########################"
    echo "# Installing Consul UI #"
    echo "########################"

    # Taken from https://www.consul.io/downloads.html
    TMP_DIR='date +%Y%m%d%H%M%S'
    mkdir $TMP_DIR
    (
        wget https://releases.hashicorp.com/consul/0.6.3/consul_0.6.3_web_ui.zip
        ZIP_FILE=`ls -1 *.zip`
        unzip $ZIP_FILE
        rm $ZIP_FILE
        mkdir $ROOT/bin/consulgui
        mv index.html $ROOT/bin/
        mv static ROOT/bin/
    )
    rm -rf $TMP_DIR
fi