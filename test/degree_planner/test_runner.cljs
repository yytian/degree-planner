(ns ^:figwheel-always degree-planner.test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [degree-planner.logic-test]))

(enable-console-print!)

(defn ^:export run []
  (run-tests 'degree-planner.logic-test))
