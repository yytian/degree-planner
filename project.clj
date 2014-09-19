;; add application caching

(defproject degree-planner "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://degree-planner.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2322"]
                 [com.cognitect/transit-cljs "0.8.188"]
                 [cljs-ajax "0.3.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [liberator "0.12.0"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [ring-basic-authentication "1.0.5"]
                 [environ "0.5.0"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [enlive "1.1.5"]
                 [om "0.7.3"]]
  :cljsbuild {
    :builds {
        :dev
        {:source-paths ["src"]
         :compiler {
            :output-to "resources/public/js/cljs-debug.js"
            :optimizations :whitespace
            :pretty-print true }}
        :prod
        {:source-paths ["src"]
         :compiler {
            :output-to "resources/public/js/cljs.js"
            :optimizations :advanced
            :pretty-print false}}}}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-cljsbuild "1.0.3"]]
  :hooks [environ.leiningen.hooks leiningen.cljsbuild]
  :main degree-planner.web
  :jvm-opts ["-Djava.awt.headless=true"]
  :source-paths ["src"]
  :uberjar-name "degree-planner-standalone.jar"
  :profiles {:production {:env {:production true}}
             :dev {:source-paths ["dev"]}})

;; https://groups.google.com/forum/#!topic/clojurescript/4Y8OWUl6u_Y
