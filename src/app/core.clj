(ns app.core
  (:require [org.httpkit.server :as http]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [datomic.api :as d]
            [hiccup2.core :as h]
            [app.db]
            [clojure.data.json :as json])
  (:gen-class))

(defn page [title children]
  [:html
   [:head
    [:title title]
    [:meta {:name :viewport :content "width=device-width, initial-scale=1"}]
    [:link {:rel :preconnect :href "https://fonts.googleapis.com"}]
    [:link {:rel :preconnect :href "https://fonts.gstatic.com" :crossorigin true}]
    [:link {:rel :stylesheet :href "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"}]
    [:link {:rel :stylesheet :href "/main.css"}]]
   [:body [:div {:style {:height "100vh"
                         :display :flex
                         :flex-direction :column
                         :gap 16
                         :align-items :center
                         :justify-content :center}}
           [:h1 "Learn Datalog"]
           [:div
            [:p "Learn how to query a Datomic database with Datalog"]
            [:p "@todo About the Schema"]
            children]]
    [:script {:type "module" :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@1.0.0-RC.8/bundles/datastar.js"}]]])

(defn run-q [req]
  (let [body (json/read-str (slurp (:body req)))
        q (get body "input")
        conn (app.db/scratch-conn)]
    (app.db/setup-db conn)
    (str (d/q q (d/db conn)))))

(defn runnable []
  [:div {"data-signals:output" "'...'" "data-signals:input" "'[:find ?n :where [?e :pokemon/name ?n]]'"}
   [:textarea {:name "input"
               :class "input"
               "data-bind" "input"
               :style {:height 200 :width 300}}
    "[:find ?n :where [?e :pokemon/name ?n]]"]
   [:button {"data-on:click" "@post('/api/q')"} "run"]
   [:textarea {:name "output"
               :class "output"
               "data-text" "$output"
               :style {:height 200 :width 300}} ""]])

(defroutes routes
  ;; In a real system, you would serve static files from a CDN
  (route/files "/" {:root "public"})
  ;; Api routes
  (POST "/api/q" req
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {"output" (run-q req)})})
  (GET "/" req
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html
                 (page "title" (runnable))))}))

(defn run-server []
  (let [port (or (some-> (System/getenv "PORT") parse-long)
                 8080)]
    (println (str "Server is listening on: http://localhost:" port))
    (http/run-server #'routes {:port port})))

(defn -main [& args]
  (run-server))

(comment
  (def stop-server (-main))
  (stop-server))
