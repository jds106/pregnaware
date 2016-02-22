#/bin/bash

# Runs a simple local webserver - see https://www.npmjs.com/package/local-web-server
ws -p 8680 -d ./src/main/resources --rewrite '/login -> /html/login.html' --rewrite '/main -> /html/main.html'