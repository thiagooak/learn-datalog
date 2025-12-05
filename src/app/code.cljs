(ns app.code
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