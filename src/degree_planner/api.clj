(ns degree-planner.api
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]))

(def departments ["AMATH" "CO" "CS" "MATH" "PMATH" "STAT"])
