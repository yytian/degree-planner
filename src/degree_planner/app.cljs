(ns degree-planner.app
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [degree-planner.logic :as logic]
            [clojure.string :as string]
            [ajax.core :refer [GET]]))

(defonce app-state (atom {:courses nil
                          :course-input nil
                          :department-input nil
                          :department-input-valid false
                          :program nil}))

(defonce departments nil)

(GET "api/departments" {:handler #(set! departments (set %))})

(defn set-state! [key new-value]
  (do
    (swap! app-state #(assoc % key new-value))
    (rerender!)))

(defn transform-state! [key transform]
  (do
    (swap! app-state #(let [old-value (key %)]
                        (assoc % key (transform old-value))))
    (rerender!)))

(defn text-input [key {id :id placeholder :placeholder maxLength :maxLength change-hook :change-hook}]
  (d/input {:type "text" :id id :className "form-control" :placeholder placeholder :maxLength maxLength
            :onChange (fn [e] (let [v (.-value (.-target e))]
                                (do (if-not (nil? change-hook) (change-hook v))
                                  (set-state! key v))))}))

(q/defcomponent CourseView [course]
  (d/div {:className "course row"}
         (d/span nil (str (name (:id course)) ": " (:name course)))
         (d/button {:type "button" :className "btn btn-warning pull-right" :onClick (fn [e] (transform-state! :courses #(disj % course)))} "Delete")))

(q/defcomponent CoursesView [[courses input-valid]]
  (d/div {:id "courses-view" :className "col-md-6 rows-container"}
         (apply d/div {:className "courses"} (map CourseView courses))
         (d/div {:className "input-group row"}
                (text-input :department-input {:placeholder "Choose a department" :maxLength 5
                                               :change-hook #(set-state! :department-input-valid
                                                                         (contains? departments (string/upper-case %)))})
                (let [icon-class (if input-valid "glyphicon glyphicon-ok" "glyphicon glyphicon-remove")]
                  (d/span {:className (str "input-group-addon " icon-class)}))
                (text-input :course-input {:placeholder "Enter a course number"}))))

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
         (SolutionsView (logic/check-program program (set (map :id courses))))))

(q/defcomponent RootView [app]
  (d/div {:id "root" :className "row"}
         (CoursesView [(:courses app) (:department-input-valid app)])
         (ProgramView [(:program app) (:courses app)])))

(defn rerender! []
  (q/render (RootView @app-state)
            (.getElementById js/document "my-app")))

(GET "api/courses/CS" {:handler #(set-state! :courses (set (take 10 %)))})

(GET "api/programs/BCS" {:handler #(set-state! :program (logic/definition->program %))})
