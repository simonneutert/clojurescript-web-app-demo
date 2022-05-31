FROM clojure:tools-deps-1.11.1.1113-slim-buster AS build

WORKDIR /app
COPY deps.edn prod.edn /app/
COPY src /app/src
RUN clj -M -m cljs.main \
    --optimizations advanced \
    -co prod.edn \
    -c interactive-events-mainz.core

FROM nginx:1-alpine

COPY --from=build /app/out /usr/share/nginx/html/out
COPY index.html /usr/share/nginx/html/