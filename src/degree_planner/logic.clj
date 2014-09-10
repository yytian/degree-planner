(ns degree-planner.web
  (:require [clojure.core.logic :as logic]))

(defrecord Program [title constraints])

(defrecord Constraint [description rule])

(defmacro one-of [list]
  `(Constraint. {:one-of ~list}
                #(clojure.set/intersection (set %) (set ~list))))

(defmacro all-of [list]
  `(Constraint. {:all-of ~list}
                #(clojure.set/superset? (into #{} %) (into #{} ~list))))

(def bcs (Program. "Bachelor of Computer Science"
                   (vector
                    (one-of "CS 1x5 series" ['CS115 'CS135 'CS145])
                    (one-of "CS 1x6 series" ['CS136 'CS146])
                    (one-of "Calculus 1" ['MATH127 'MATH137 'MATH147])
                    (one-of "Calculus 2" ['MATH128 'MATH138 'MATH148])
                    (one-of "Algebra" ['MATH135 'MATH145])
                    (one-of "Linear Algebra 1" ['MATH136 'MATH146])
                    (one-of "Introduction to Combinatorics" ['MATH239 'MATH249])
                    (one-of "Probability" ['STAT230 'STAT240])
                    (one-of "Statistics" ['STAT231 'STAT241])
                    (all-of "Computer Science core" ['CS240 'CS241 'CS245 'CS246 'CS251 'CS341 'CS350]))))


(every? #((:rule %) ['CS135 'CS136 'MATH135 'MATH136 'MATH137 'MATH138 'MATH249 'STAT240 'STAT231 'CS240 'CS241 'CS245 'CS246 'CS251 'CS341 'CS350]) (:constraints bcs))

(all-of ['CS115 'CS135])
