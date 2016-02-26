#!/bin/bash

# Updates the NGINX installation and reloads the config

if [ -e '/etc/nginx/sites-enabled/default' ]; then
    echo "Removing default NGINX site"
    sudo rm /etc/nginx/sites-enabled/default
fi

echo "Installing server configuration"
sudo cp ../etc/nginx/pregnaware /etc/nginx/sites-enabled/

echo "Reloading configuration"
sudo nginx -s reload

echo
echo "Config reloaded - logs available at:"
echo "      /var/log/nginx/access.log"
echo "      /var/log/nginx/error.log"
echo
