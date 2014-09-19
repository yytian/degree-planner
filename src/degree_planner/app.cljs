(ns degree-planner.app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [degree-planner.logic :as logic]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(def app-state (atom {:courses #{"Lion" "Zebra" "Buffalo" "Antelope"}}))

(defn course-view [course owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [delete]}]
                  (dom/li #js {:className "course"}
                          (dom/span nil course)
                          (dom/button #js {:onClick (fn [e] (put! delete course))} "Delete")))))

(defn add-course [app owner]
  (let [new-course (-> (om/get-node owner "new-course")
                       .-value)]
    (when new-course
      (om/transact! app :courses #(conj % new-course)))))

(defn courses-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
                {:delete (chan)})
    om/IWillMount
    (will-mount [_]
                (let [delete (om/get-state owner :delete)]
                  (go (loop []
                        (let [course (<! delete)]
                          (om/transact! app :courses
                                        #(disj % course))
                          (recur))))))
    om/IRenderState
    (render-state [this {:keys [delete]}]
                  (dom/div nil
                           (dom/input #js {:id "course-form"})
                           (apply dom/ul #js {:id "course-list"}
                                  (om/build-all course-view (:courses app)
                                                {:init-state {:delete delete}}))
                           (dom/div nil
                                    (dom/input #js {:type "text" :ref "new-course"})
                                    (dom/button #js {:onClick #(add-course app owner)} "Add course"))
                           (dom/div nil
                                    (dom/span nil (reduce str (map :id logic/cs-courses))))))))

(om/root courses-view
         app-state
         {:target (. js/document (getElementById "my-app"))})
