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
    [:link {:rel :stylesheet :href "https://cdn.jsdelivr.net/npm/syntax-highlight-element@1/dist/themes/prettylights.min.css"}]
    [:link {:rel :stylesheet :href "/main.css"}]]
   [:body children
    [:script {:type "module" :src "https://cdn.jsdelivr.net/npm/syntax-highlight-element@1/+esm"}]
    [:script {:src "/syntax-highlight-config.js"}]
    [:script {:type "module" :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.8/bundles/datastar.js"}]]])

(def runnable-counter (atom 0))

(defn runnable [input]
  (let [random-name (swap! runnable-counter inc)
        input-name  (str "in" random-name)
        output-name (str "out" random-name)]

    [:div {(str "data-signals:" input-name) (str "'" input "'")
           (str "data-signals:" output-name) "',,,'"}
     [:syntax-highlight {:language "clojure"
                         :contenteditable "plaintext-only"
                         :class "input"
                         :spellcheck "false"
                         ;; adding new lines breaks the syntax highlight
                         "data-on-signal-patch" "el.update()"
                         "data-on-signal-patch-filter" (str "{include: /^" input-name "$/}")
                         "data-on:input" (str "$" input-name " = el.contentElement.innerText")}
      (with-out-str (pprint input))]

     [:div
      [:button {"data-on:click__prevent" (str "@post('/api/q', {filterSignals: {include: /^" input-name "|" output-name "$/}})")
                :style {:margin "5px"}}
       "Run"]
      [:button {"data-on:click__prevent" (str "$" output-name " = ',,,'")
                :style {:margin "5px"}}
       "Reset"]]

     [:syntax-highlight {:language "clojure"
                         :name output-name
                         :class "output"
                         "data-on-signal-patch" "el.update()"
                         "data-on-signal-patch-filter" (str "{include: /^" output-name "$/}")
                         "data-text" (str "$" output-name)}]]))
