(ns degree-planner.app
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [degree-planner.logic :as logic]
            [degree-planner.data :as data]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [cljs.reader :as reader]
            [cljsjs.pako :as pako]
            [cemerick.url :as url]
            [ajax.core :refer [GET]]))

(defonce ugrad-calendar-link "https://ugradcalendar.uwaterloo.ca/page/")
(defonce ugrad-courses-link "http://www.ucalendar.uwaterloo.ca/1516/COURSE/")

(defonce app-state (atom {:planned-course-ids nil
                          :show-planned true
                          :show-help false
                          :course-number-input ""
                          :department-input ""
                          :program nil
                          :course-defs-by-dept {}
                          :course-defs-by-id {}}))

(declare rerender!)

; Persistent data (not reset on figwheel reload) which does not have to be in root atom
(defonce departments data/departments)
(defonce course-defs-by-dept nil)
(defonce course-defs-by-id nil)
(defonce sorted-course-list nil)

(when (nil? course-defs-by-dept)
  (doseq [dept departments]
    (let [courses ((keyword dept) data/courses)]
      (set! course-defs-by-dept (assoc course-defs-by-dept (keyword dept) courses false))
      (doseq [course courses]
        (set! course-defs-by-id (assoc course-defs-by-id (:id course) course) false))))
  (set! sorted-course-list (->> (vals course-defs-by-dept) (apply concat) (sort-by :id) (take 20))))

(defn get-state [key]
  (key @app-state))

(defn set-state! [key new-value to-render]
  (do
    (swap! app-state #(assoc % key new-value))
    (when to-render (rerender!))))

(defn transform-state! [key transform to-render]
  (do
    (swap! app-state #(let [old-value (key %)]
                        (assoc % key (transform old-value))))
    (when to-render (rerender!))))

(defn text-input [key {id :id placeholder :placeholder maxLength :maxLength change-hook :change-hook to-render :to-render}]
  (d/input {:type "text" :id id :className "form-control" :placeholder placeholder :maxLength maxLength
            :onChange (fn [e] (let [v (.-value (.-target e))]
                                (if (nil? change-hook) (set-state! key v to-render) (set-state! key (change-hook v) to-render))))}))

(defn course-search-filter [search-string planned course]
  (and (every? identity (map = (seq (name (:id course))) (seq search-string)))
       (not (contains? planned (:id course)))))

(q/defcomponent CourseView [[selected course-id]]
  (let [course (course-id course-defs-by-id)]
    (d/div {:className "course row"}
           (d/a {:href (str ugrad-courses-link "course-" (:department course) ".html#" (name course-id))} (str (name course-id) ": "))
           (d/span nil (:name course))
           (if selected
             (d/button {:type "button" :className "btn btn-warning pull-right"
                        :onClick (fn [e] (transform-state! :planned-course-ids #(disj % course-id) true))} "Delete")
             (d/button {:type "button" :className "btn btn-primary pull-right"
                        :onClick (fn [e] (transform-state! :planned-course-ids #(conj % course-id) true))} "Select")))))

(q/defcomponent SearchView [[planned department-input course-number-input]]
  (if (contains? departments department-input)
    (let [dept-courses (get course-defs-by-dept (keyword department-input))
          search-results (filter (partial course-search-filter (str department-input course-number-input) planned) dept-courses)]
      (apply d/div {:id "search-results"} (map #(CourseView [false (:id %)]) search-results)))
    (when (string/blank? department-input)
      (apply d/div {:id "search-results"} (map #(CourseView [false (:id %)]) sorted-course-list)))))

(q/defcomponent CoursesView [[show-planned planned department-input course-number-input]]
  (d/div {:id "courses-view" :className "col-md-6 rows-container"}
         (when show-planned
           (apply d/div {:className "courses"} (map #(CourseView [true %]) (sort planned))))
         (d/div {:className "input-group row"}
                (text-input :department-input {:placeholder "Choose a department" :maxLength 5 :change-hook string/upper-case :to-render true})
                (let [icon-class (if (contains? departments (string/upper-case department-input))
                                   "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")]
                  (d/span {:className (str "input-group-addon " icon-class)}))
                (text-input :course-number-input {:placeholder "Enter a course number" :maxLength 4 :to-render true}))
         (SearchView [planned department-input course-number-input] course-defs-by-dept)))

(q/defcomponent SolutionView [solution]
  (let [icon-class (if (:satisfied solution) "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")
        icon (d/span {:className (str "leading-icon " icon-class)})
        row-class (if (:satisfied solution) "success" "failure")]
    (d/div {:className (str "solution row " row-class)}
           icon
           (d/span nil (:title solution) ": ")
           (let [course-ids (map name (:course-set solution))
                 to-print (string/join ", " course-ids)]
             (d/span nil to-print)))))

(q/defcomponent SolutionsView [solutions]
  (apply d/div {:className "solutions"} (map SolutionView solutions)))

(q/defcomponent ConditionView [[planned condition]]
  (let [satisfied (logic/check-condition condition planned)
        icon-class (if satisfied "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")
        icon (d/span {:className (str "leading-icon " icon-class)})
        row-class (if satisfied "success" "failure")]
    (d/div {:className (str "condition row " row-class)}
           icon
           (d/span nil (:title condition))
           (apply d/div {:className "indent"} (map #(ConditionView [planned %]) (:subconditions condition))))))

(q/defcomponent ConditionsView [[planned conditions]]
  (apply d/div {:className "conditions"} (map #(ConditionView [planned %]) conditions)))

(q/defcomponent ProgramView [[program planned]]
  (d/div {:id "program-view" :className "col-md-6"}
         (d/div nil (d/a {:href (str ugrad-calendar-link (:link program))} (:title program)))
         (d/div {:className "row"}
                "Constraints:"
                (SolutionsView (logic/check-constraints (:constraints program) planned)))
         (d/div {:className "row"}
                "Conditions:"
                (ConditionsView [planned (:conditions program)]))))

(q/defcomponent NavView [[show-help planned]]
  (d/nav {:className "navbar navbar-default navbar-fixed-top"}
         (d/div {:className "container-fluid"}
                (d/button {:type "button" :className "btn btn-default navbar-btn"
                           :onClick (fn [e] (transform-state! :show-planned not true))} "Show/hide planned courses")
                (d/button {:type "button" :className "btn btn-default navbar-btn"
                           :onClick (fn [e] (transform-state! :show-help not true))} "Show/hide help")
                (d/a {:type "button" :className "btn btn-default navbar-btn"
                      :href (str (-> js/window .-location .-href)
                                 "?courses="
                                 (.deflate js/pako (pr-str planned) (clj->js {:raw true})))}
                     "Permanent Link")
                (d/a {:type "button" :className "btn btn-default navbar-btn" :href "https://github.com/~yytian"}
                     "Source Code"))
         (when show-help
           (d/div {:className "alert alert-info"}
                  "Degree requirements are separated into two types: constraints and conditions."
                  (d/br)
                  "The difference is that constraints are satisfied by assigning courses to them, and each course can only be assigned to one constraint."
                  (d/br)
                  "On the other hand, each condition is independent, and considers the entire set of planned courses."
                  (d/br)
                  "If it is possible to satisfy all constraints, the algorithm will do so. If it is not possible, constraints will be greedily satisfied (ie. possibly with a suboptimal solution). If any constraints are unsatisfied, it indicates that the current set of planned courses is insufficient."
                  (d/br)
                  (d/br)
                  "To save course selections, simply save the link provided by the Permanent Link button."
                  (d/br)
                  "Feedback can be sent to the author by email at yytian@uwaterloo.ca."))))

(q/defcomponent RootView [app]
  (d/div {:id "root"}
         (NavView [(:show-help app) (:courses app)])
         (d/div {:className "row"}
                (CoursesView [(:show-planned app) (:planned-course-ids app) (:department-input app) (:course-number-input app)])
                (ProgramView [(:program app) (:planned-course-ids app)]))))

(defn rerender! []
  (q/render (RootView @app-state)
            (.getElementById js/document "my-app")))

(defn query-data->course-ids [data]
  (let [query-array (clj->js (string/split data ","))
        inflated-array ((-> js/window .-Array .-from) (.inflate js/pako query-array (clj->js {:raw true})))
        inflated-list (js->clj inflated-array)
        inflated-string (->> inflated-list (map char) (apply str))]
    (reader/read-string inflated-string)))

(when (nil? (get-state :planned-course-ids))
  (let [url (url/url (-> js/window .-location .-href))
        query-data (get (:query url) "courses")]
    (if (string/blank? query-data)
      (set-state! :planned-course-ids data/default-course-selection true)
      (set-state! :planned-course-ids (query-data->course-ids query-data) true))))

(when (nil? (get-state :program))
  (set-state! :program (logic/definition->program data/bcs) true))

(rerender!)
