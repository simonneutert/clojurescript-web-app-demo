#!/bin/sh

rm -rf .clj-kondo .calva .cpcache .lsp out

# Backup and refetch production json
#
# cp events.json events.backup_$(date +"%Y-%m-%d-%H%M%S").json
# curl -o events.json https://prod.appframework.de/static_proxy/vrm_events_new.json

clj -M -m cljs.main \
    --optimizations advanced \
    -co prod.edn \
    -c interactive-events-mainz.core

rm -rf .clj-kondo .calva .cpcache .lsp

echo "build complete"