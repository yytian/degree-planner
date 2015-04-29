(ns degree-planner.app
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]))

(defonce app-state (atom {:courses #{"CS135" "CS136" "MATH135" "MATH136"}
                      :new-course ""}))

(defn transform-state [key transform]
  (swap! app-state #((let [old-value (key %)]
                       (assoc % key (transform old-value))))))

(q/defcomponent CourseView [course]
  (d/div {:className "course"}
        (d/span nil course)
        (d/button {:className "btn btn-b btn-sm smooth"
              :onClick (fn [e] (transform-state :courses #(disj % course)))} "Delete")))

(q/defcomponent CoursesView [courses]
  (d/div {:id "courses-view"}
         (apply d/ul {:className "courses"} (map CourseView courses))
         (d/div nil
                (d/input {:type "text" :class "smooth" :ref "new-course"
                          :onKeyDown (fn [e] (let [v (.-value (.-target e))]
                                               (transform-state :new-course (constantly v))))}))))

(q/defcomponent RootView [app]
  (d/div {:id "root"}
         (CoursesView (:courses @app))))

(q/render (RootView app-state)
          (.getElementById js/document "my-app"))
