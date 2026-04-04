(ns app.ui
  (:require [clojure.pprint :refer [pprint]]))

(defn page [title children]
  [:html
   [:head
    [:title title]
    [:meta {:charset "UTF-8"}]
    [:meta {:name :viewport :content "width=device-width, initial-scale=1"}]
    [:link {:rel :preconnect :href "https://fonts.googleapis.com"}]
    [:link {:rel :preconnect :href "https://fonts.gstatic.com" :crossorigin true}]
    [:link {:rel :stylesheet :href "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"}]
    [:link {:rel :stylesheet :href "/main.css"}]]
   [:body children
    [:script {:type "module" :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.8/bundles/datastar.js"}]]])

(defn runnable [input]
  (let [random-name (int (* 100 (rand)))
        input-name  (str "in" random-name)
        output-name (str "out" random-name)]
    [:form
     [:textarea {:name input-name
                 :class "input"
                 "data-bind" input-name
                 :spellcheck "false"}
      (with-out-str (pprint input))]
     [:textarea {:name output-name
                 :class "output"
                 :readonly "readonly"
                 :spellcheck "false"
                 "data-text" (str "$" output-name)}]
     [:div
      [:button {"data-on:click" "@post('/api/q', {contentType: 'form'})"
                :style {:margin "5px"}}
       "Run"]
      [:button {"data-on:click__prevent" (str "$" output-name "=''")
                :style {:margin "5px"}}
       "Reset"]]]))
