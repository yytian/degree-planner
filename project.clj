;; add application caching

(defproject degree-planner "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://student.cs.uwaterloo.ca/~yytian"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.cognitect/transit-cljs "0.8.220"]
                 [cljs-ajax "0.3.14"]
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
  :plugins [[lein-cljsbuild "1.0.6"]]
  :source-paths ["src"])
