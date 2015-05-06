;; add application caching

(defproject degree-planner "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://degree-planner.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.cognitect/transit-cljs "0.8.207"]
                 [cljs-ajax "0.3.11"]
                 [clj-http "1.1.1"]
                 [liberator "0.12.0"]
                 [io.clojure/liberator-transit "0.3.0"]
                 [compojure "1.3.3"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring-basic-authentication "1.0.5"]
                 [environ "1.0.0"]
                 [enlive "1.1.5"]
                 [quiescent "0.2.0-alpha1"]]
  :cljsbuild {
    :builds {
        :dev
        {:source-paths ["src"]
         :compiler {
            :output-to "resources/public/js/cljs-debug.js"
            :optimizations :whitespace
            :pretty-print true }}
        :production
        {:source-paths ["src"]
         :compiler {
            :output-to "resources/public/js/cljs.js"
            :optimizations :advanced
            :pretty-print false}}}}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-cljsbuild "1.0.5"]]
  :hooks [environ.leiningen.hooks]
  :profiles {:uberjar {:aot :all}}
  :main degree-planner.web
  :jvm-opts ["-Djava.awt.headless=true"]
  :source-paths ["src"]
  :uberjar-name "degree-planner-standalone.jar")
