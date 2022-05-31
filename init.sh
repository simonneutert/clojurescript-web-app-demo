#!/bin/sh

# cp events.json events.backup_$(date +"%Y-%m-%d-%H%M%S").json
# curl -o events.json  https://prod.appframework.de/static_proxy/vrm_events_new.json

curl -o events.json  https://prod.appframework.de/static_proxy/vrm_events_new.json