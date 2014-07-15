(ns degree-planner.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes ANY]]))

(def departments ["AMATH" "CO" "CS" "MATH" "PMATH" "STAT"])

(defroutes app
  (ANY "/api/:dept" [dept] (resource :available-media-types ["text/html"]
                                 :handle-ok (str "<html>" (slurp (str "data/courses/" dept "-courses.edn")) "</html>"))))

(def handler
  (-> app
      (wrap-params)))

(defonce server (run-jetty #'handler {:port 8080 :join? false}))

(.start server)
(.stop server)
