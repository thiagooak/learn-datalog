(ns app.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]
            [datomic.api :as d]
            [hiccup2.core :as h]
            [hiccup.page :as p]
            [app.db]
            [app.ui]
            [app.content])
  (:gen-class))

(defn run-q [q]
  (let [conn (app.db/scratch-conn)]
    (app.db/setup-db conn)
    ;; running arbitrary user generated code
    ;; directly in the database is pretty safe, right?
    (d/q q (d/db conn))))

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
       :body (json/write-str {out-name
                              (-> in
                                  (run-q)
                                  (pprint)
                                  (with-out-str))})}))
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (h/html
                 {:mode :html}
                 (p/doctype :html5)
                 (app.ui/page "Learn Datalog" (app.content/content-home))))}))

(defn run-server [port]
  (println (str "Server is listening on: http://localhost:" port))
  (http/run-server (wrap-params #'routes) {:port port}))

(defn -main [& args]
  (let [port (or (some-> (System/getenv "PORT") parse-long)
                 (some-> (first args) parse-long)
                 8080)]
    (run-server port)))

(comment
  (def stop-server (-main))
  (stop-server))
