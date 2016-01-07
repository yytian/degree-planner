# degree-planner

A web app which allows UW students to check whether their list of courses taken satisfies degree requirements. Currently, only the Bachelor of Computer Science degree is supported.

## Building
This project is written in [ClojureScript](https://github.com/clojure/clojurescript) and managed using [Leiningen](https://github.com/technomancy/leiningen), which must be installed to build the project.

To deploy the web app, run the command:

    $ lein cljsbuild once prod

This will run a production build (with compiler optimizations on, etc), outputting files to the resources/public directory. One can then serve this directory as normal static resources using any web server, and access the application at `index.html`.

For a more development-oriented workflow, run the command:

    $ lein figwheel
    
This starts a [Figwheel](https://github.com/bhauman/lein-figwheel) instance, which deploys a development build using a local server. The application can then be accessed at http://localhost:3449/dev.html. Figwheel will watch all `.cljs` source files, and when any are changed will recompile with the changes, which should be dynamically reflected in the application. It will also run the test suite on every recompile.

## License

Copyright © 2016 Jim Tian

Distributed under the MIT license (see License.md).
