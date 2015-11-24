;; add application caching

(defproject degree-planner "0.2.0"
  :description "Tool for checking UWaterloo degree requirements"
  :url "http://student.cs.uwaterloo.ca/~yytian"
  :license {:name "MIT"
  :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.cognitect/transit-cljs "0.8.225"]
                 [cljs-ajax "0.3.14"]
                 [enlive "1.1.5"]
                 [quiescent "0.2.0-RC2"]]
  :clean-targets [:target-path "out" "resources/public/js"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {:main "degree-planner.app"
              :asset-path "js/out"
              :output-to "resources/public/js/cljs-debug.js"
              :output-dir "resources/public/js/out"
              :optimizations :none
              :pretty-print true
              :source-map true
              :source-map-timestamp true
              :cache-analysis true }
              :figwheel true }
             {:id "prod"
              :source-paths ["src"]
              :compiler {:main "degree-planner.app"
              :asset-path "js/out"
              :output-to "resources/public/js/cljs.js"
              :output-dir "resources/public/js/out"
              :optimizations :advanced
              :pretty-print false}}]}
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]
  :source-paths ["src"])
