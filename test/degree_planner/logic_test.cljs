(ns ^:figwheel-load degree-planner.logic-test
  (:require [cljs.test :refer-macros [deftest is]]
            [degree-planner.logic :refer [all-satisfied? definition->program check-program]]
            [degree-planner.data :refer [bcs default-course-selection]]))

(def prog (definition->program bcs))

; This doesn't work as a function for some reason
(defmacro none-satisfied? [solutions]
  "Checks whether a list of Solutions has any satisfied"
  (every? (false? :satisfied) solutions))

(defn strict-every? [pred coll]
  (and (seq coll) (every? pred coll)))

(defn constraint-satisfied? [solutions constraint-name]
  (->> solutions
       (filter #(= (:title %) constraint-name))
       (strict-every? :satisfied)))

(defn as-expected? [solutions [constraint-name expectation]]
  (= (constraint-satisfied? solutions constraint-name) expectation))

(defn these-satisfied? [solutions expected]
  (and (= (count solutions) (count expected))
       (every? (partial as-expected? solutions) (seq expected))))

(deftest bcs-empty
  (is (none-satisfied? (check-program prog []))))

(deftest bcs-default
  (is (these-satisfied? (check-program prog default-course-selection)
                        {
                         "CS first course" true
                         "CS second course" true
                         "Calculus 1" false
                         "Calculus 2" false
                         "Algebra" true
                         "Linear Algebra 1" false
                         "Intro Combinatorics" true
                         "Probability" false
                         "Statistics" true
                         "Computer Science core" true
                         "Three additional CS courses chosen from CS 340-398, 440-489" false
                         "Two additional CS courses chosen from CS 440-489" false
                         "One additional course" false
                         "Systems and SE" false
                         "Applications" false
                         "Mathematical foundations of CS" false
                         }
                        )))
