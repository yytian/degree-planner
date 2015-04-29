(ns scraper
  (:require [clojure.string :as string])
  (:require [clojure.java.io :as io])
  (:require [clj-http.client :as client])
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn courses [dep]
  (let [body (:body (client/get (str "http://www.ucalendar.uwaterloo.ca/1415/COURSE/course-" dep ".html")))]
    (map #(first (html/html-snippet %)) (next (re-seq #"<center>.*?</center>" body)))))

(defn get-text [tag]
  (str (first (:content (first tag)))))

(defn process-offerings [line]
  (apply list (filter identity (map #(case % \F "Fall" \W "Winter" \S "Spring" nil) (seq line)))))

(defn read-course [course dep]
  { :department dep
    :id (keyword (:name (:attrs (first (html/select course [[html/first-child :tr] :a])))))
    :name (get-text (html/select course [[(html/nth-child 2) :tr] :b]))
    :description (string/replace (get-text (html/select course [[(html/nth-child 3) :tr] :td])) #"\[.*\]" "")
    :offerings (process-offerings (re-find #"Offered:.*\]" (pr-str (:content course))))
  })

(defn scrape []
  (doseq [dep ["CS" "MATH" "CO" "PMATH" "STAT" "AMATH"]]
    (with-open [wrtr (io/writer (str "data/courses/" dep "-courses.edn"))]
      (.write wrtr (pr-str (map #(read-course % dep) (courses dep)))))))
