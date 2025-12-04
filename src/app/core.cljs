(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]))

(defn fetch-pokemon-names []
  (-> (js/fetch "/api/pokemon-names")
      (.then #(.text %))
      (.then #(js/alert %))))

(defui app []
  ($ :div {:style {:height "100vh"
                   :display :flex
                   :flex-direction :column
                   :gap 16
                   :align-items :center
                   :justify-content :center}}
     ($ :h1 "Hello, UIx!")
     ($ :button {:on-click fetch-pokemon-names}
        "fetch pokemon names")))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root
   ($ uix/strict-mode
      ($ app))
   root))

(defn ^:export init []
  (render))
