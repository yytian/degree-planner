(ns degree-planner.api
  (:require [liberator.core :refer [resource defresource]]
            [clojure.java.io :as io]
            [clojure.set :refer [union]]
            [io.clojure.liberator-transit]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
            [ring.util.response :as resp]))

(def departments #{"AMATH" "CO" "CS" "MATH" "PMATH" "STAT"})

(def course-lists (reduce
                   #(assoc %1 %2 (read-string (slurp (str "resources/data/courses/" %2 "-courses.edn"))))
                   {}
                   departments))

(defn course-range [source left right]
  (set (->> source (map :id) (filter #(<= (compare % right) 0 (compare % left))))))

(def bcs {:title "Bachelor of Computer Science"
          :rules (let [core #{:CS240 :CS241 :CS245 :CS246 :CS251 :CS341 :CS350}
                       cs-courses (get course-lists "CS")]
                   (vector
                    [:one-of "CS 1x5 series" #{:CS115 :CS135 :CS145}]
                    [:one-of "CS 1x6 series" #{:CS136 :CS146}]
                    [:one-of "Calculus 1" #{:MATH127 :MATH137 :MATH147}]
                    [:one-of "Calculus 2" #{:MATH128 :MATH138 :MATH148}]
                    [:one-of "Algebra" #{:MATH135 :MATH145}]
                    [:one-of "Linear Algebra 1" #{:MATH136 :MATH146}]
                    [:one-of "Introduction to Combinatorics" #{:MATH239 :MATH249}]
                    [:one-of "Probability" #{:STAT230 :STAT240}]
                    [:one-of "Statistics" #{:STAT231 :STAT241}]
                    [:all-of "Computer Science core" core]
                    [:n-of "Three additional CS courses chosen from CS 340-398, 440-489"
                     (union (course-range cs-courses :CS340 :CS398) (course-range cs-courses :CS440 :CS489)) {:n 3}]
                    [:n-of "Two additional CS courses chosen from CS 440-489" (course-range cs-courses :CS440 :CS489) {:n 2}]
                    ))})

(defroutes app
  (GET "/api/courses/:dept" [dept] (resource :available-media-types ["application/transit+json"
                                                                     "application/transit+msgpack"
                                                                     "application/json"]
                                             :handle-ok (get course-lists dept)))
  (GET "/api/programs/:program" [program] (resource :available-media-types ["application/transit+json"
                                                                            "application/transit+msgpack"
                                                                            "application/json"]
                                                    :handle-ok bcs))
  (GET "/" [] (-> (resp/resource-response "index.html") (resp/content-type "text/html")))
  (GET "/dev" [] (-> (resp/resource-response "dev.html") (resp/content-type "text/html")))
  (GET "/js/:script" [script] (-> (resp/resource-response (str "public/js/" script ".js")) (resp/content-type "application/javascript")))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))
