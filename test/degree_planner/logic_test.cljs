(ns ^:figwheel-load degree-planner.logic-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [degree-planner.logic :refer [definition->program check-constraints check-condition]]
            [degree-planner.data :refer [bcs default-course-selection]]))

(def bcs-program (definition->program bcs))

(defn strict-every? [pred coll]
  (and (seq coll) (every? pred coll)))

(defn find-by-title [coll title]
  (let [results (filter #(= (:title %) title) coll)]
    (if (= (count results) 1)
      (first results)
      (throw (js/Error. (str "Expected one titled " name ", but found " (count results) "."))))))

(defn test-constraints [constraints planned-courses expectations]
  (let [solutions (check-constraints constraints planned-courses)]
    (testing "for constraints"
      (testing "to have the expected number of solutions"
        (is (count solutions) (count expectations)))
      (doseq [[constraint-name expected-val] expectations]
              (testing (str "to be " expected-val " for " constraint-name)
                (is (= (:satisfied (find-by-title solutions constraint-name)) expected-val)))))))

(defn test-conditions [conditions planned-courses expectations]
  (testing "for conditions"
    (testing "to have the expected number" (is (count conditions) (count expectations)))
    (doseq [[condition-name expected-val] expectations]
      (let [condition (find-by-title conditions condition-name)
              subconditions (:subconditions condition)]
        (if (vector? expected-val)
          ;; Case where expected val is [value subcondition-expectations]
          (testing (str "to be " (first expected-val) " for " condition-name)
            (is (= (check-condition condition planned-courses) (first expected-val)))
            (test-conditions subconditions planned-courses (second expected-val)))
          ;; Case where expected val is plain value
          (testing (str "to be " expected-val " for " condition-name)
            (is (= (check-condition condition planned-courses) expected-val))))))))

(defn test-plan-on-program [program planned-courses constraints-expectations conditions-expectations]
  "Must be used within a deftest."
  (test-constraints (:constraints program) planned-courses constraints-expectations)
  (test-conditions (:conditions program) planned-courses conditions-expectations))

(deftest bcs-empty
  (test-plan-on-program bcs-program []
      {
       "CS first course" false
       "CS second course" false
       "Calculus 1" false
       "Calculus 2" false
       "Algebra" false
       "Linear Algebra 1" false
       "Intro Combinatorics" false
       "Probability" false
       "Statistics" false
       "Computer Science core" false
       "Three additional CS courses chosen from CS 340-398, 440-489" false
       "Two additional CS courses chosen from CS 440-489" false
       "One additional course" false
       }
      {
       "Two of the following" [false {"Systems and SE" false
                                      "Applications" false
                                      "Mathematical foundations of CS" false}]
       }
      ))

(deftest bcs-default
  (test-plan-on-program bcs-program default-course-selection
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
                         }
                        {
                         "Two of the following" [false {"Systems and SE" false
                                      "Applications" false
                                      "Mathematical foundations of CS" false}]
                         }
                        ))

(deftest bcs-complete
  (test-plan-on-program bcs-program #{:CS145 :CS146 :MATH145 :MATH146 :MATH147 :MATH148 :MATH235 :MATH247
                                      :MATH249 :CO255 :STAT230 :STAT241 :CS240 :CS241 :CS245 :CS246 :CS251
                                      :CS341 :CS350 :CS365 :CS371 :CS444 :CS452 :CS454 :CS466}
                        {
                         "CS first course" true
                         "CS second course" true
                         "Calculus 1" true
                         "Calculus 2" true
                         "Algebra" true
                         "Linear Algebra 1" true
                         "Intro Combinatorics" true
                         "Probability" true
                         "Statistics" true
                         "Computer Science core" true
                         "Three additional CS courses chosen from CS 340-398, 440-489" true
                         "Two additional CS courses chosen from CS 440-489" true
                         "One additional course" true
                         }
                        {
                         "Two of the following" [true {"Systems and SE" true
                                      "Applications" false
                                      "Mathematical foundations of CS" true}]
                         }
                        ))
