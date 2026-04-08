(ns app.content
  (:require [app.ui]
            [clojure.pprint :refer [pprint]]))

(defn content-home []
  [:div
   [:h1 "Learn Datalog"]
   [:p "This interactive website will help you learn how to query a Datomic databases using Datalog."]
   [:p "Click the \"Run\" button below the code block to run your first Datalog query."]
   (app.ui/runnable [:find '?name :where
                     ['?entity :pokemon/type "Grass"]
                     ['?entity :pokemon/name '?name]])
   [:p "Try editing the query above to return Pokemon of other types like Fire or Electric."]
   #_[:h2 "Data model"]
   [:p "Before we continue querying the database, let's take a look at our data model."]
   [:code-highlighter {:language "clojure"}
    (with-out-str
      (pprint {:pokemon/name    "Bulbasaur",
               :pokemon/number  "001",
               :pokemon/type    ["Grass" "Poison"],
               :stat/attack     49,
               :stat/defense    49,
               :stat/hp         45,
               :stat/sp-attack  65,
               :stat/sp-defense 65,
               :stat/speed      45}))]
   [:p "The map above represents one Pokemon Entity. Each key of the map (:stat/speed, :pokemon/number, :pokemon/name, etc) represents one Attribute and each value of the map (45, \"001\", \"Bulbasaur\", etc) represents one or more Values."]
   #_[:p "It is important to understand the EAV (Entity, Attribute, Value) structure as we use it to contruct our Datalog queries."]
   [:p "Each Entity has an entity-id. Let's find \"Bulbasaur\"'s entity-id"]
   (app.ui/runnable [:find '?entity-id
                     :where ['?entity-id :pokemon/name "Bulbasaur"]])
   [:p "Now let's list all of the Attributes associated with \"Bulbasaur\""]
   (app.ui/runnable [:find '?attribute
                     :where
                     ['?entity-id :pokemon/name "Bulbasaur"]
                     ['?entity-id '?attribute-id '_]
                     ['?attribute-id :db/ident '?attribute]])
   [:p "Finally, let's list all of the Attributes and values associated with \"Bulbasaur\""]
   (app.ui/runnable [:find '?attribute '?value
                     :where
                     ['?entity-id :pokemon/name "Bulbasaur"]
                     ['?entity-id '?attribute-id '?value]
                     ['?attribute-id :db/ident '?attribute]])
   [:p "Read Datomic's "
    [:a {:href "https://docs.datomic.com/whatis/data-model.html#universal"} "Universal Schema"] " documentation to learn more about Datomic's data model"]
   [:p "Take a look at the "
    [:a {:href "https://github.com/thiagooak/learn-datalog/blob/main/resources/pokemon.edn#L34"} "file"]
    " defining all of the Pokemon in our database."]
   [:h2 "Querying"]
   (app.ui/runnable '[:find ?name ?type :where
                     (not [?entity :pokemon/type "Grass"])
                     [?entity :pokemon/name ?name]
                     [?entity :pokemon/type ?type]])

   (app.ui/runnable '[:find ?name ?speed :where
                      [?entity :pokemon/name ?name]
                      [?entity :stat/speed ?speed]
                      [(> ?speed 80)]])
   ])
