(ns degree-planner.logic
  (:require [clojure.core.logic :as logic]
            [clojure.edn :refer [read-string]]))

;; https://groups.google.com/forum/#!topic/clojure/6UFheRkptbE
(defn not-membero
  [x l]
  (logic/fresh [head tail]
         (logic/conde
          ((logic/== l ()))
          ((logic/conso head tail l)
           (logic/!= x head)
           (not-membero x tail)))))

(defrecord Program [title constraints])

(defrecord Constraint [title rules pred])

(defmacro one-of [title list]
  `(Constraint. ~title
                [:one-of ~list]
                #(clojure.set/intersection (set %) (set ~list))))

(defmacro all-of [title list]
  `(Constraint. ~title
                [:all-of ~list]
                #(clojure.set/intersection (set %) (set ~list))))

(defmacro n-courses [title n pred]
  `(Constraint. ~title
                [:n-courses ~n]
                pred))

(def cs-courses (clojure.edn/read-string (slurp (str "data/courses/CS-courses.edn"))))

(defmacro course-range [source left right]
  `(->> ~source (map :id) (filter #(<= (compare % ~right) 0 (compare % ~left)))))

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
                                     #(logic/run 1 [a b c y z]
                                                (logic/membero a combined)
                                                (logic/membero a %)
                                                (logic/membero b combined)
                                                (logic/membero b %)
                                                (logic/membero c combined)
                                                (logic/membero c %)
                                                (logic/membero y cs440-489)
                                                (logic/membero y %)
                                                (logic/membero z cs440-489)
                                                (logic/membero z %)
                                                (logic/distincto [a b c y z])))
                                   )
                      ))))


(map #((:pred %) [:CS135 :CS136 :MATH135 :MATH136 :MATH137 :MATH138 :MATH249 :STAT240 :STAT231 :CS240 :CS241 :CS245 :CS246 :CS251 :CS341 :CS350]) (:constraints bcs))

(let [cs340-398 (course-range cs-courses :CS340 :CS398)
                                         cs440-489 (course-range cs-courses :CS440 :CS489)
                                         combined (concat cs340-398 cs440-489)
       temp [:CS485 :CS442 :CS452 :CS489]]
                                     (logic/run 1 [a b c y z]
                                                  (logic/membero a combined)
                                                  (logic/membero a temp)
                                                  (logic/membero b combined)
                                                  (logic/membero b temp)
                                                  (logic/membero c combined)
                                                  (logic/membero c temp)
                                                  (logic/membero y cs440-489)
                                                  (logic/membero y temp)
                                                  (logic/membero z cs440-489)
                                                  (logic/membero z temp)
                                                  (logic/distincto [a b c y z])))
