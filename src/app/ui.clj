(ns app.ui
  (:require [clojure.pprint :refer [pprint]]
            [hiccup2.core :as h]
            [hiccup.page :as p]))

(defn nav-li [m]
  [:li [:a {:href (str "/" (key m))} (:nav-title (val m))]])

(defn nav [chapters]
  [:nav [:ul
         (map nav-li chapters)]])

(defn page [title nav children]
  (str
   (h/html
    {:mode :html}
    (p/doctype :html5)
    [:html
     [:head
      [:title title]
      [:meta {:charset "UTF-8"}]
      [:meta {:name :viewport :content "width=device-width, initial-scale=1"}]
      [:link {:rel :preconnect :href "https://fonts.googleapis.com"}]
      [:link {:rel :preconnect :href "https://fonts.gstatic.com" :crossorigin true}]
      [:link {:rel :stylesheet :href "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"}]
      [:link {:rel :stylesheet :href "https://cdn.jsdelivr.net/npm/@thiago.oak/code-highlighter@latest/prettylights.css"}]
      [:link {:rel :stylesheet :href "/main.css"}]]
     [:body
      nav
      [:main children]
      [:script {:src "https://cdn.jsdelivr.net/npm/prismjs@1.30.0/components/prism-core.min.js" :data-manual "data-manual"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/prismjs@1.30.0/components/prism-clojure.min.js"}]
      [:script {:src "https://cdn.jsdelivr.net/npm/prismjs@1.30.0/components/prism-sql.min.js"}]
      [:script {:type "module" :src "https://cdn.jsdelivr.net/npm/@thiago.oak/code-highlighter@latest/code-highlighter.js"}]
      [:script {:type "module" :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.8/bundles/datastar.js"}]]])))

(def runnable-counter (atom 0))

(defn runnable [dataset input]
  (let [random-name (swap! runnable-counter inc)
        input-name  (str "in" random-name)
        output-name (str "out" random-name)]

    [:div {(str "data-signals:" input-name) (str "'" input "'")
           (str "data-signals:" output-name) "',,,'"}
     [:code-highlighter {:language "clojure"
                         :contenteditable "plaintext-only"
                         :class "input"
                         :spellcheck "false"
                         "data-on-signal-patch" "el.highlight()"
                         "data-on-signal-patch-filter" (str "{include: /^" input-name "$/}")
                         "data-on:input" (str "$" input-name " = el.innerText")}
      (with-out-str (pprint input))]

     [:div
      [:button {"data-on:click__prevent" (str "@post('/api/q?dataset=" dataset "', {filterSignals: {include: /^" input-name "|" output-name "$/}})")
                :style {:margin "5px"}}
       "Run"]
      [:button {"data-on:click__prevent" (str "$" output-name " = ',,,'")
                :style {:margin "5px"}}
       "Reset"]]

     [:code-highlighter {:language "clojure"
                         :name output-name
                         :class "output"
                         "data-on-signal-patch" "el.highlight()"
                         "data-on-signal-patch-filter" (str "{include: /^" output-name "$/}")
                         "data-text" (str "$" output-name)}]]))

(defn code
  ([input]
   (code "clojure" (with-out-str (pprint input))))
  ([lang input]
   [:code-highlighter {:language lang :class "input"} input]))