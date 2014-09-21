(ns degree-planner.logic
  (:require [cognitect.transit :as transit]
            [ajax.core :refer [GET]]
            [clojure.set :refer [intersection difference union]]))

(defrecord Program [title constraints])

(defrecord Constraint [title render])

(defn match-into-sets [input num sets]
  (if (and (seq input) (seq sets) (> num 0))
    (let [matches (intersection input (first sets))]
      (if (>= (count matches) num)
        (set (take num matches))
        (union matches (match-into-sets (difference input matches) (- num (count matches)) (rest sets)))))
    #{}))

(def rule-type-map
  {:one-of (fn [params] #(let [result (intersection % params)]
                           [(seq result) result]))
   :all-of (fn [params] #(let [result (intersection % params)]
                           [(= result params) result]))
   :n-of (fn [[num & sets]] #(match-into-sets % num sets))
   })

(defn rule->constraint [[type title params]]
  (Constraint. title ((type rule-type-map) params)))

(def cs-courses [])

(GET "api/courses/CS" {:handler #(set! cs-courses %) })
