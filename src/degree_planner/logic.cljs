(ns degree-planner.logic
  (:require [cognitect.transit :as transit]
            [degree-planner.combinatorics :as combo]
            [cljs.core.match :refer-macros [match]]
            [ajax.core :refer [GET]]
            [clojure.set :refer [intersection difference union subset?]]))

(defrecord Program [title renderer])

(defrecord Constraint [title generator])

(defrecord Solution [title satisfied course-set])

(defn has-failures? [solutions]
  (every? identity (map :satisfied solutions)))

(defn try-combinations [planned-courses title combinations constraints]
  (match [combinations constraints]
         ; no combinations or constraints
         [([] :seq) ([] :seq)] '()
         ; no combinations
         [([] :seq) _] (conj (solve planned-courses constraints) (Solution. title false #{}))
         ; no constraints
         [([comb & combs] :seq) ([] :seq)] (Solution. title true comb) ; just take first comb, any will work
         ; both combinations and constraints
         [([comb & combs] :seq) _]
           (let [remaining-courses (difference planned-courses comb) ; after choosing first comb
                 first-combination-solutions (solve remaining-courses constraints)]
             (if (has-failures? first-combination-solutions)
               (recur planned-courses title combs constraints) ; backtrack to other combs
               (conj first-combination-solutions (Solution. title true comb))))))

(defn solve [planned-courses constraints]
  (if (empty? constraints)
    '()
    (let [first-constraint-combinations ((:generator (first constraints)) planned-courses)]
      (try-combinations planned-courses
                        (:title (first constraints))
                        first-constraint-combinations
                        (rest constraints)))))

(defn rule->generator [rule-type course-set params]
  (match [rule-type course-set params]
         [:one-of _ _] #(intersection course-set %)
         [:all-of _ _] #(if (subset? course-set %) course-set #{})
         [:n-of _ {:n n}] #((combo/combinations (intersection course-set %) n))))

(defn rule->constraint [[rule-type title course-set & params]]
  (Constraint. title (rule->generator rule-type course-set params)))

(defn definition->program [definition]
  (Program. (:title definition)
            (fn [planned-courses]
              (let [constraints (map rule->constraint (:rules definition))]
                (solve planned-courses constraints)))))

(def cs-courses)
(def bcs)

(GET "api/courses/CS" {:handler #(set! cs-courses %) })
(GET "api/programs/BCS" {:handler #(set! bcs %) })
