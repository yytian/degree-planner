(ns degree-planner.web
  (:require [degree-planner.api :refer [app]]
            [compojure.handler :refer [site]]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (slurp (io/resource "500.html"))}))))

(defn -main [& [port]]
  (let [port (Integer. (or port 5000))]
    (jetty/run-jetty app {:port port :join? false})))

;; For interactive development:
;; (.start server)
;; (.stop server)
;; (def server (-main))
