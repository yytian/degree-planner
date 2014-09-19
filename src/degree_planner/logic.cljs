(ns degree-planner.logic
  (:require [cognitect.transit :as transit]
            [ajax.core :refer [GET POST]]
            [clojure.set]))

(defrecord Program [title constraints])

(defrecord Constraint [title rules pred])

(defn one-of [title list]
  (Constraint. title
               [:one-of list]
               #(clojure.set/intersection (set %) (set list))))

(defn all-of [title list]
  (Constraint. title
               [:all-of list]
               #(clojure.set/intersection (set %) (set list))))

;; ranges should be a vector of vectors starting with a number (indicating number of courses to take) followed by courses
;; there's probably a way to do it with destructuring
(defn from-ranges [title ranges]
  (Constraint. title
               ranges
               :placeholder))

(def cs-courses [])

(GET "api/CS" {:handler #(set! cs-courses %) })

(defn course-range [source left right]
  (->> source (map :id) (filter #(<= (compare % right) 0 (compare % left)))))

(def bcs (Program. "Bachelor of Computer Science"
                   (let [core [:CS240 :CS241 :CS245 :CS246 :CS251 :CS341 :CS350]]
                     (vector
                      (one-of "CS 1x5 series" [:CS115 :CS135 :CS145])
                      (one-of "CS 1x6 series" [:CS136 :CS146])
                      (one-of "Calculus 1" [:MATH127 :MATH137 :MATH147])
                      (one-of "Calculus 2" [:MATH128 :MATH138 :MATH148])
                      (one-of "Algebra" [:MATH135 :MATH145])
                      (one-of "Linear Algebra 1" [:MATH136 :MATH146])
                      (one-of "Introduction to Combinatorics" [:MATH239 :MATH249])
                      (one-of "Probability" [:STAT230 :STAT240])
                      (one-of "Statistics" [:STAT231 :STAT241])
                      (all-of "Computer Science core" core)
                      (Constraint. "Three additional CS courses chosen from CS 340-398, 440-489; two additional CS courses chosen from CS 440-489."
                                   [:n-courses 3]
                                   (let [cs340-398 (course-range cs-courses :CS340 :CS398)
                                         cs440-489 (course-range cs-courses :CS440 :CS489)
                                         combined (concat cs340-398 cs440-489)]
                                     :placeholder)
                                   )
                      ))))


(map #((:pred %) [:CS135 :CS136 :MATH135 :MATH136 :MATH137 :MATH138 :MATH249 :STAT240 :STAT231 :CS240 :CS241 :CS245 :CS246 :CS251 :CS341 :CS350]) (:constraints bcs))
