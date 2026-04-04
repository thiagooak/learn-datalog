(ns app.content
  (:require [app.ui]))

(defn content-home []
  [:div
   [:h1 "Learn Datalog"]
   [:p "This interactive website will help you learn how to query a Datomic databases using Datalog."]
   [:p "Let’s start with a query that returns the names of all grass type pokemon."]
   (app.ui/runnable "[:find ?name
 :where [?entity :pokemon/type \"Grass\"]
        [?entity :pokemon/name ?name]]")
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
   (app.ui/runnable "[:find ?entity-id
 :where [?entity-id :pokemon/name \"Ivysaur\"]]")
   [:p "The query above returns the entity id of the entity representing the pokemon."]
   [:p "Now let's list all of the attributes associated with that entity and their values"]
   (app.ui/runnable "[:find ?attribute-id ?value
 :where [?entity-id :pokemon/name \"Ivysaur\"]
        [?entity-id ?attribute-id ?value]]")
   [:p "Notice that we got the attibute ids and their values, let's update the query to get the attribute names instead"]
   (app.ui/runnable "[:find ?attribute ?value
 :where [?entity-id :pokemon/name \"Ivysaur\"]
        [?entity-id ?attribute-id ?value]
        [?attribute-id :db/ident ?attribute]]")
  ;; universal schema (https://docs.datomic.com/whatis/data-model.html#universal)
   ])
