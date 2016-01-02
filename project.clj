;; add application caching

(defproject degree-planner "0.2.0"
  :description "Tool for checking UWaterloo degree requirements"
  :url "http://student.cs.uwaterloo.ca/~yytian"
  :license {:name "MIT"
  :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [cljsjs/pako "0.2.7-0"] ; Thank god someone did the interop so that I don't have to!
                 [cljs-ajax "0.3.14"]
                 [com.cemerick/url "0.1.1"]
                 [enlive "1.1.5"]
                 [quiescent "0.2.0-RC2"]]
  :clean-targets [:target-path "out" "resources/public/js"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {:main "degree-planner.app"
              :asset-path "js/dev"
              :output-to "resources/public/js/cljs-debug.js"
              :output-dir "resources/public/js/dev"
              :optimizations :none
              :pretty-print true
              :source-map true
              :source-map-timestamp true
              :cache-analysis true }
              :figwheel true }
             {:id "prod"
              :source-paths ["src"]
              :compiler {:main "degree-planner.app"
              :asset-path "js/prod"
              :output-to "resources/public/js/cljs.js"
              :output-dir "resources/public/js/prod"
              :optimizations :advanced
              :pretty-print false}}]}
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]
  :source-paths ["src"])
