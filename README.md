# fully functional everything clojurescript example

> When you walk to the edge of all the light you have and take that first step into the darkness of the unknown, you must believe that one of two things will happen. There will be something solid for you to stand upon or you will be taught to fly.”  
> \- Patrick Overton, The leaning tree

---

I wanted to see how far I can push it, having dabbled with Clojure/-Script for a few weeks after work.

So, there you have it: _**an example of an artisinal (most probably bad 🫠) NodeJS-independent Frontend Project 🍻 😎**_

> what's in for me learning clojure(-script?)  
> \- everyone not writing lisp

well, if you thought [Elm](https://elm-lang.org/) was cool, but you struggled with the types... or maybe you think types hinder from greatness... [Rich never said this](https://www.youtube.com/watch?v=2V1FtfBDsLU&t=2227s), but I heard it anyways in his talks.

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

## Pre-Conditions (if you want to get your hands dirty 👏)

- have [Clojure](https://clojure.org/guides/install_clojure) installed
- skim this page about quick starting ClojureScript: https://clojurescript.org/guides/quick-start
- [curl](https://github.com/curl/curl)

_I code clojure in VS Code using_ [VSCode with Calva](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva) or a similar Editor supporting Clojure(-Script)

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

## Development (with VSCode and Calva)

- start a REPL `deps.edn + ClojureScript build for Browser`
- a Browser will open, then
- evaluate `core.cljs` line by line 
- evaluate requirements in `events.cljs` and inspect the events in state
