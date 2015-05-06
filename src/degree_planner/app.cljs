(ns degree-planner.app
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [degree-planner.logic :as logic]
            [clojure.string :as string]
            [ajax.core :refer [GET]]))

; TODO: replace "courses" with "course-ids"

(defonce app-state (atom {:courses nil
                          :show-planned false
                          :course-number-input ""
                          :department-input ""
                          :program nil
                          :course-defs-by-dept {}
                          :course-defs-by-id {}}))

(defonce departments nil)
(defonce course-defs-by-dept {})
(defonce course-defs-by-id {})

(defn set-state! [key new-value to-render]
  (do
    (swap! app-state #(assoc % key new-value))
    (when to-render (rerender!))))

(defn transform-state! [key transform to-render]
  (do
    (swap! app-state #(let [old-value (key %)]
                        (assoc % key (transform old-value))))
    (when to-render (rerender!))))

(GET "api/departments"
     {:handler (fn [depts]
                 (do (set! departments (set depts))
                   (doseq [dept depts]
                     (GET (str "api/courses/" dept)
                          {:handler (fn [courses]
                                      (do
                                        (set! course-defs-by-dept (assoc course-defs-by-dept (keyword dept) courses) false)
                                        (doseq [course courses]
                                          (set! course-defs-by-id (assoc course-defs-by-id (:id course) course) false))))}))))})

(defn text-input [key {id :id placeholder :placeholder maxLength :maxLength change-hook :change-hook to-render :to-render}]
  (d/input {:type "text" :id id :className "form-control" :placeholder placeholder :maxLength maxLength
            :onChange (fn [e] (let [v (.-value (.-target e))]
                                (if (nil? change-hook) (set-state! key v to-render) (set-state! key (change-hook v) to-render))))}))

(defn course-search-filter [search-string courses course]
  (and (every? identity (map = (seq (name (:id course))) (seq search-string)))
       (not (contains? courses (:id course)))))

(q/defcomponent CourseView [[selected course-id]]
  (d/div {:className "course row"}
         (d/span nil (str (name course-id) ": " (:name (course-id course-defs-by-id))))
         (if selected
           (d/button {:type "button" :className "btn btn-warning pull-right"
                      :onClick (fn [e] (transform-state! :courses #(disj % course-id) true))} "Delete")
           (d/button {:type "button" :className "btn btn-primary pull-right"
                      :onClick (fn [e] (transform-state! :courses #(conj % course-id) true))} "Select"))))

(q/defcomponent SearchView [[courses department-input course-number-input]]
  (when (contains? departments department-input)
    (let [dept-courses (get course-defs-by-dept (keyword department-input))
          search-results (filter (partial course-search-filter (str department-input course-number-input) courses) dept-courses)]
      (apply d/div {:id "search-results"} (map #(CourseView [false (:id %)]) search-results)))))

(q/defcomponent CoursesView [[show-planned courses department-input course-number-input]]
  (d/div {:id "courses-view" :className "col-md-6 rows-container"}
         (when show-planned
           (apply d/div {:className "courses"} (map #(CourseView [true %]) (sort courses))))
         (d/div {:className "input-group row"}
                (text-input :department-input {:placeholder "Choose a department" :maxLength 5 :change-hook string/upper-case :to-render true})
                (let [icon-class (if (contains? departments (string/upper-case department-input))
                                   "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")]
                  (d/span {:className (str "input-group-addon " icon-class)}))
                (text-input :course-number-input {:placeholder "Enter a course number"}))
         (SearchView [courses department-input course-number-input] course-defs-by-dept)))

(q/defcomponent SolutionView [solution]
  (let [icon-class (if (:satisfied solution) "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")
        icon (d/span {:className (str "leading-icon " icon-class)})
        row-class (if (:satisfied solution) "success" "failure")]
    (d/div {:className (str "solution row " row-class)}
           icon
           (d/span nil (:title solution))
           (d/span nil (string/join ", " (:course-set solution))))))

(q/defcomponent SolutionsView [solutions]
  (apply d/div {:className "solutions"} (map SolutionView solutions)))

(q/defcomponent ProgramView [[program courses]]
  (d/div {:id "program-view" :className "col-md-6"}
         (d/div nil (:title program))
         (SolutionsView (logic/check-program program courses))))

(q/defcomponent NavView []
  (d/nav {:className "navbar navbar-default navbar-fixed-top"}
         (d/div {:className "container-fluid"}
                (d/button {:type "button" :className "btn btn-default navbar-btn"
                           :onClick (fn [e] (transform-state! :show-planned not true))} "Show/hide planned courses")
                (d/label {:className "btn btn-default" :htmlFor "import"}
                         ; thanks http://stackoverflow.com/questions/11235206/twitter-bootstrap-form-file-element-upload-button/25053973#25053973
                         (d/input {:type "file" :id "import" :style {:display "none"}} "Import"))
                (d/button {:type "button" :className "btn btn-default navbar-btn"
                           :onClick (fn [e] (.exportNewWindow js/window
                                                              (str (println-str ";; Download this file for later import") \newline
                                                                   (pr-str (map :id (:courses @app-state))))))}
                          "Export"))))

(q/defcomponent RootView [app]
  (d/div {:id "root"}
         (NavView)
         (d/div {:className "row"}
                (CoursesView [(:show-planned app) (:courses app) (:department-input app) (:course-number-input app)])
                (ProgramView [(:program app) (:courses app)]))))

(defn rerender! []
  (q/render (RootView @app-state)
            (.getElementById js/document "my-app")))

(set-state! :courses #{:CS135 :CS136 :MATH135 :MATH239 :STAT231 :CS240 :CS241 :CS245 :CS246 :CS251 :CS341 :CS350} true)

(GET "api/programs/BCS" {:handler #(set-state! :program (logic/definition->program %) true)})
