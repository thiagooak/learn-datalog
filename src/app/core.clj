(ns app.core
  (:require [org.httpkit.server :as http]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [datomic.api :as d]
            [hiccup2.core :as h]
            [hiccup.page :as p]
            [app.db]
            [clojure.edn :as edn]
            [clojure.data.json :as json])
  (:gen-class))

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

(defn run-q [q]
  (let [conn (app.db/scratch-conn)]
    (app.db/setup-db conn)
    (str (d/q q (d/db conn)))))

(defn runnable [input]
  (let [random-name (int (* 100 (rand)))
        input-name  (str "in" random-name)
        output-name (str "out" random-name)]
    [:form {(str "data-signals:" output-name) "'...'"
           (str "data-signals:" input-name) (str \' input \')}
    [:textarea {:name input-name
                :class "input"
                "data-bind" input-name
                :style {:height "200px" :width "300px"}}]
    [:button {"data-on:click" "@post('/api/q', {contentType: 'form'})"} "run"]
    [:textarea {:name output-name
                :class "output"
                "data-text" (str "$" output-name)
                :style {:height "200px" :width "300px"}}]]))

(defn content-home []
  [:div [:p "This interactive website will help you learn how to query a Datomic databases using Datalog."]
  [:p "Let’s start with a query that returns the names of all grass type pokemon."]
  (runnable "[:find ?name :where [?entity :pokemon/type \"Grass\"] [?entity :pokemon/name ?name]]")
  [:p "You can edit the query above to find other types like Fire or Electric."]
  [:h2 "Data model"]
  [:p "Below we have one vector that contains three of the maps that we used to populate our database."]
  [:pre [:code "[{:pokemon/name    \"Bulbasaur\",
  :pokemon/number  \"001\",
  :pokemon/type    [\"Grass\" \"Poison\"],
  :stat/attack     49,
  :stat/defense    49,
  :stat/hp         45,
  :stat/sp-attack  65,
  :stat/sp-defense 65,
  :stat/speed      45}

 {:pokemon/name    \"Ivysaur\",
  :pokemon/number  \"002\",
  :pokemon/type    [\"Grass\" \"Poison\"],
  :stat/attack     62,
  :stat/defense    63,
  :stat/hp         60,
  :stat/sp-attack  80,
  :stat/sp-defense 80,
  :stat/speed      60}

 {:pokemon/name    \"Venusaur\",
  :pokemon/number  \"003\",
  :pokemon/type    [\"Grass\" \"Poison\"],
  :stat/attack     82,
  :stat/defense    83,
  :stat/hp         80,
  :stat/sp-attack  100,
  :stat/sp-defense 100,
  :stat/speed      80}]"]]

  [:p "You can find the full list here https://github.com/thiagooak/learn-datalog/blob/main/resources/pokemon.edn#L34"]
  [:p "Each map represents one Pokemon Entity and defines various Attributes with their respective Values."]
  [:p "It is important to understand what EAV (Entity, Attribute, Value) represents when working with Datomic, so let's run some queries to make it more concrete."]
  (runnable "[:find ?entity :where [?entity :pokemon/name \"Ivysaur\"]]")
  ;; universal schema (https://docs.datomic.com/whatis/data-model.html#universal)
  ])

(defn page-home []
  (page "Learn Datalog" [:div [:h1 "Learn Datalog"]
                         [:div (content-home)]]))

(defroutes routes
  ;; In a real system, you would serve static files from a CDN
  (route/files "/" {:root "public"})
  ;; Api routes
  (POST "/api/q" req
    ;; do not rely on first and second
    (let [in-name (first (keys (:params req)))
          in  (get-in req [:params in-name])
          out-name (second (keys (:params req)))]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {out-name (run-q in)})}))
  (GET "/" req
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html {:mode :html}
                        (p/doctype :html5) (page-home)))}))

(defn run-server []
  (let [port (or (some-> (System/getenv "PORT") parse-long)
                 8080)]
    (println (str "Server is listening on: http://localhost:" port))
    (http/run-server (wrap-params #'routes) {:port port})))

(defn -main [& args]
  (run-server))

(comment
  (def stop-server (-main))
  (stop-server))
