(ns degree-planner.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [liberator.core :refer [resource defresource]]
            [io.clojure.liberator-transit]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [ring.util.response :as resp]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]))

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))

(def course-lists (reduce
                   #(assoc %1 %2 (read-string (slurp (str "resources/data/courses/" %2 "-courses.edn"))))
                   {}
                   ["AMATH" "CS" "CO" "MATH" "PMATH" "STAT"]))

(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))
  (ANY "/api/:dept" [dept] (resource :available-media-types ["application/transit+json"
                                                             "application/transit+msgpack"
                                                             "application/json"]
                                     :handle-ok (get course-lists dept)))
  (GET "/" [] (-> (resp/resource-response "index.html") (resp/content-type "text/html")))
  (GET "/js/:script" [script] (-> (resp/resource-response (str "public/js/" script ".js")) (resp/content-type "application/javascript")))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
;; (.start server)
;; (.stop server)
;; (def server (-main))
