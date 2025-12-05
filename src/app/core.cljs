(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]))

(defn run-q [input set-output]
  (-> (js/fetch "/api/q"
                #js {:method "POST"
                     :headers #js {"Content-Type" "application/json"}
                     :body (js/JSON.stringify #js {"q" input})})
      (.then #(.text %))
      (.then #(set-output %))))

(defui runnable [props]
  (let [[input set-input] (uix/use-state (:q props))
        [output set-output] (uix/use-state "")]
    ($ :div
     ($ :textarea {:name "input"
                   :class "input"
                   :style {:height 200 :width 300}
                   :default-value input
                   :on-change #(set-input (.-value (.-target %)))})
     ($ :button {:on-click #(run-q input set-output)} "run")
     ($ :textarea {:name "output"
                   :class "output"
                   :style {:height 200 :width 300}
                   :default-value output}))))

(defui app []
  ($ :div {:style {:height "100vh"
                   :display :flex
                   :flex-direction :column
                   :gap 16
                   :align-items :center
                   :justify-content :center}}
     ($ :h1 "Learn Datalog")
     ($ runnable {:q "[:find ?n
 :where [?e :pokemon/name ?n]]"})))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root
   ($ uix/strict-mode
      ($ app))
   root))

(defn ^:export init []
  (render))
