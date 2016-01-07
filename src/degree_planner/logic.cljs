(ns degree-planner.logic
  (:require [degree-planner.combinatorics :as combo]
            [cljs.core.match :refer-macros [match]]
            [clojure.set :refer [intersection difference union subset?]]))

(defrecord Program [title link constraints conditions])
(defrecord Constraint [title generator])
(defrecord Condition [title checker subconditions])
(defrecord Solution [title satisfied course-set])

(defn all-satisfied? [solutions]
  "Checks whether a list of Solutions has any unsatisfied"
  (every? :satisfied solutions))

(declare solve)

; Try to make the algorithm cleaner if at all possible

(defn try-combinations [planned-courses title combinations constraints]
  "Returns a list of Solutions given a list of planned courses, the name of the current Constraint, the viable combinations for the current Constraint, and a list of remaining Constraints."
  (match [combinations constraints]
         ; no combinations or constraints
         [([] :seq) ([] :seq)] (list (Solution. title false #{}))
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
               (if (or (all-satisfied? first-combination-solutions) (empty? combs))
                   (conj first-combination-solutions (Solution. title true comb))
                   (recur planned-courses title combs constraints)))
             (recur planned-courses title combs constraints))))

(defn solve [planned-courses constraints]
  "Returns a list of Solutions given lists of planned courses and Constraints."
  (if (empty? constraints)
    '()
    (let [first-constraint-combinations ((:generator (first constraints)) planned-courses)]
      (try-combinations planned-courses
                        (:title (first constraints))
                        first-constraint-combinations
                        (rest constraints)))))

(defn make-generator [rule-type constituents params]
  "Generators take in a list of planned courses and return a (possibly lazy) list of combinations that are viable for the associated Constraint."
  (match [rule-type constituents params]
         [:one-of _ _] #(combo/split-set (intersection constituents %))
         [:all-of _ _] #(if (subset? constituents %) (list constituents) '())
         [:n-of _ {:n n}] #(map set (combo/combinations (intersection constituents %) n))))

(defn make-constraint [[rule-type title constituents params]]
  "Constraints are rules which must be satisfied by a course set, where no course can be used in more than one Constraint in a set of Constraints."
  (Constraint. title (make-generator rule-type constituents params)))

(defn count-courses-contained [planned-courses course-set]
  (count (filter #(contains? planned-courses %) course-set)))

(defn count-conditions-met [planned-courses conditions]
  (count (->> conditions (map #((:checker %) planned-courses)) (filter identity))))

(defn make-condition [[rule-type title constituents params]]
  "Conditions are rules which must be satisfied by a course set, but which does not affect other Conditions or Constraints.
  Checkers take in a list of planned courses and returns its viability for the associated Condition."
  (match [rule-type constituents params]
         [:one-of _ _] (Condition. title #(>= (count-courses-contained % constituents) 1) [])
         [:all-of _ _] (Condition. title #(>= (count-courses-contained % constituents) (count constituents)) [])
         [:n-of _ {:n n}] (Condition. title #(>= (count-courses-contained % constituents) n) [])
         [:meta-n-of condition-defs {:n n}] (let [subconditions (map make-condition condition-defs)]
                                              (Condition. title #(>= (count-conditions-met % subconditions) n) subconditions))))

(defn definition->program [definition]
  (Program. (:title definition)
            (:link definition)
            (map make-constraint (:constraints definition))
            (map make-condition (:conditions definition))))

(defn check-constraints [constraints planned-courses]
  "Returns list of Solutions for a list of Constraints given a list of planned courses"
  (solve planned-courses constraints))

(defn check-condition [condition planned-courses]
  "Returns the boolean result for a Condition given a list of planned courses"
  ((:checker condition) planned-courses))
