(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.content]))

(defui app []
  ($ :div {:style {:height "100vh"
                   :display :flex
                   :flex-direction :column
                   :gap 16
                   :align-items :center
                   :justify-content :center}}
     ($ :h1 "Learn Datalog")
     ($ app.content/content)))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root
   ($ uix/strict-mode
      ($ app))
   root))

(defn ^:export init []
  (render))
