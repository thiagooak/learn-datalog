(ns app.content
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.code :as c]))

(defui content []
  ($ :div
     ($ :p "Learn how to query a Datomic database with Datalog")
     ($ :p "@todo About the Schema")
     ($ c/runnable {:q "[:find ?n
 :where [?e :pokemon/name ?n]]"})))