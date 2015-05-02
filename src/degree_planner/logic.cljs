(ns degree-planner.logic
  (:require [cognitect.transit :as transit]
            [degree-planner.combinatorics :as combo]
            [cljs.core.match :refer-macros [match]]
            [ajax.core :refer [GET]]
            [clojure.set :refer [intersection difference union subset?]]))

(defrecord Program [title constraints])

(defrecord Constraint [title generator])

(defrecord Solution [title satisfied course-set])

(defn has-failures? [solutions]
  "Checks whether a list of Solutions has any unsatisfied"
  (not-every? identity (map :satisfied solutions)))

(defn try-combinations [planned-courses title combinations constraints]
  "Returns a list of Solutions"
  (match [combinations constraints]
         ; no combinations or constraints
         [([] :seq) ([] :seq)] '()
         ; no combinations
         [([] :seq) _] (conj (solve planned-courses constraints) (Solution. title false #{}))
         ; no constraints
         [([comb & combs] :seq) ([] :seq)]
           (if (subset? comb planned-courses)
             (list (Solution. title true comb))
             (recur planned-courses title combs constraints)) ; try next comb
         ; both combinations and constraints
         [([comb & combs] :seq) _]
           (if (subset? comb planned-courses)
             (let [remaining-courses (difference planned-courses comb) ; after choosing first comb
                   first-combination-solutions (solve remaining-courses constraints)]
               (if (has-failures? first-combination-solutions)
                 (recur planned-courses title combs constraints)
                 (conj first-combination-solutions (Solution. title true comb))))
             (recur planned-courses title combs constraints))))

(defn solve [planned-courses constraints]
  "Returns a list of Solutions given lists of planned courses and constraints"
  (if (empty? constraints)
    '()
    (let [first-constraint-combinations ((:generator (first constraints)) planned-courses)]
      (try-combinations planned-courses
                        (:title (first constraints))
                        first-constraint-combinations
                        (rest constraints)))))

(defn rule->generator [rule-type course-set params]
  "Generators take in a list of planned courses and return a (possibly lazy) list of viable combinations"
  (match [rule-type course-set params]
         [:one-of _ _] #(let [viable (intersection course-set %)]
                          (if (empty? viable) '() (list viable)))
         [:all-of _ _] #(if (subset? course-set %) (list course-set) '())
         [:n-of _ {:n n}] #(map set (combo/combinations (intersection course-set %) n))))

(defn rule->constraint [[rule-type title course-set params]]
  (Constraint. title (rule->generator rule-type course-set params)))

(defn definition->program [definition]
  (Program. (:title definition)
            (map rule->constraint (:rules definition))))

(defn check-program [program planned-courses]
  (solve planned-courses (:constraints program)))

(def cs-courses)
(def bcs)
(def temp)

(GET "api/courses/CS" {:handler #(set! cs-courses %) })
(GET "api/programs/BCS" {:handler #(do (set! bcs %) (set! temp (check-program (definition->program bcs)
                                                                              (set (map :id cs-courses))))) })
