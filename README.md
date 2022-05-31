# fully functional everything clojurescript example

I wanted to see how far I can push it having dabbled with Clojure/-Script for a few weeks after work.

_**here's an example of an artisinal (most probably bad 🫠) NodeJS-independent Frontend Project😎**_

You may ask yourself:

> **what's in for me learning clojure(-script?)**

well, if you though Elm was cool, but you struggled with the types... or think types hinder from greatness... Rich never said this, but I heard it anyways.

## What you have at hand, when going through this codebase

- firing and receiving a http json request
  - json extraction
  - process data, extract information
- dynamic data binding / state management
- DOM creation/manipulation
- state management
- very basic Browser Event Management
  - user interaction
  - filtering
- parameterized build for dev/prod scenarios

## Run with Docker 🐳

`$ docker build . -t clj-events-spa`
`$ docker run --rm -p 8080:80 clj-events-spa`

then open [http://localhost:8080](http://localhost:8080)

## Pre-Conditions

- have [Clojure](https://clojure.org/guides/install_clojure) installed
- skim this page: https://clojurescript.org/guides/quick-start
- [curl](https://github.com/curl/curl)

**optional**

[VSCode with Calva](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva) or a similar Editor supporting Clojure(-Script)

## Compile the sources and view them

`./build-production.sh` and then http-serve the files

I solve that task using [node's http-server](https://www.npmjs.com/package/http-server)  
other may use [Python for that](https://docs.python.org/3/library/http.server.html)

## What's good, what could be easily improved, what did I learn?

#### improvements needed

- state management abstraction, faster filtering may (see medium low hanging fruits)

#### medium low hanging fruits

- lookup table for the calendar events
- memoizing results 🤷‍♂️

#### what was learned

- clojure(-script) isn't witchcraft and has some really nice docs/api/reference
- almost no dependencies needed 🤯
- one language to rule them all
