(ns app.core
  (:require [org.httpkit.server :as http]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [hiccup2.core :as h]
            [hiccup.page :as p]
            [app.db]
            [app.ui]
            [app.content])
  (:gen-class))

(defn find-fns [form]
  (cond
    (seq? form)
    (let [head (first form)
          called (when (symbol? head) #{head})]
      (into (or called #{})
            (mapcat find-fns form)))

    (coll? form)
    (into #{} (mapcat find-fns form))

    :else #{}))

(defn safe-q? [q]
  (let [allowed-fns #{'- '* '/ '+ '< '<= '= '> '>= 'count 'not 'not=}]
    (every? allowed-fns (find-fns (edn/read-string q)))))

(defn run-q [q]
  (when-not (safe-q? q) (throw (Exception. "Unsafe Query")))
  (let [conn (app.db/scratch-conn)]
   (app.db/setup-db conn)
   (d/query {:query q
             :timeout 500
             :args [(d/db conn)]})))

(defroutes routes
  ;; In a real system, you would serve static files from a CDN
  (route/files "/" {:root "public"})
  ;; Api routes
  (POST "/api/q" req
    ;; do not rely on first and second
    (let [body (json/read-str (slurp (:body req)))
          in-name (first (keys body))
          in  (get-in body [in-name])
          out-name (second (keys body))]
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
