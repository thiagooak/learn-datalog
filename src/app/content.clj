(ns app.content
  (:require [app.ui]))

(defn one []
  [:div
   [:h1 "Learn Datomic Datalog"]
   [:p "This interactive website will help you learn how to query a Datomic databases using Datalog."]
   [:p "Click the \"Run\" button below to run your first Datomic Datalog query."]
   (app.ui/runnable "pokemon"
                    [:find '?name :where
                     ['?entity :pokemon/type "Grass"]
                     ['?entity :pokemon/name '?name]])
   [:p "Try editing the query above to return Pokemon of other types like Fire or Electric."]
   [:p [:a {:href "/modeling-data"} "Next"]]])

(defn two []
  [:div
   [:h1 "Modeling Data"]
   [:p "To understand Datomic's data model, we will rewrite a simple SQL database using Datomic."]
   [:p "The database tracks our friends' favorite food and drink."]
   [:div {:style {:display "flex"}}

    [:div [:p "Friends Table"]

     [:table
      [:tr [:th "id"] [:th "first_name"] [:th "last_name"]]
      [:tr [:td "1"] [:td "Helena"] [:td "Almeida"]]
      [:tr [:td "2"] [:td "Alice"] [:td "Campos"]]
      [:tr [:td "3"] [:td "Laura"] [:td "Ferreira"]]
      [:tr [:td "4"] [:td "Miguel"] [:td "Melo"]]
      [:tr [:td "5"] [:td "Arthur"] [:td "Ramos"]]
      [:tr [:td "6"] [:td "Noah"] [:td "Silva"]]]]

    [:div [:p "Likes Table"]
     [:table
      [:tr [:th "id"] [:th "friend_id"] [:th "type"] [:th "object"]]
      [:tr [:td "10"] [:td "1"] [:td "food"] [:td "pizza"]]
      [:tr [:td "11"] [:td "2"] [:td "food"] [:td "sushi"]]
      [:tr [:td "12"] [:td "3"] [:td "food"] [:td "pizza"]]
      [:tr [:td "13"] [:td "4"] [:td "food"] [:td "pizza"]]
      [:tr [:td "14"] [:td "5"] [:td "food"] [:td "tacos"]]
      [:tr [:td "15"] [:td "6"] [:td "food"] [:td "curry"]]
      [:tr [:td "16"] [:td "1"] [:td "drink"] [:td "beer"]]
      [:tr [:td "17"] [:td "2"] [:td "drink"] [:td "wine"]]
      [:tr [:td "18"] [:td "3"] [:td "drink"] [:td "water"]]
      [:tr [:td "19"] [:td "4"] [:td "drink"] [:td "water"]]
      [:tr [:td "20"] [:td "5"] [:td "drink"] [:td "beer"]]
      [:tr [:td "21"] [:td "6"] [:td "drink"] [:td "beer"]]]]]


   [:p "To get the names of friends who like pizza, we can run a query like:"]


   (app.ui/code "sql" "SELECT f.first_name FROM friends f
 LEFT JOIN likes l ON f.id = l.friend_id
 WHERE l.type = \"food\"
 AND l.object = \"pizza\"

 -- Helena, Laura, Miguel")

   [:p "Let's rewrite this database using Datomic."]

   [:p "To start, let's define the structure of our data."]

   [:p "We can't use the two tables we had before. Datomic has a "
    [:a {:href "https://docs.datomic.com/whatis/data-model.html#universal"} "Universal Schema"]
    ". Think of it as one big table with five columns that stores " [:strong "everything"] " in our Database. For now, let's focus on three of the five columns."]

   [:ol
    [:li [:strong "Entity"] " identifies the \"thing\" we are referring to"]
    [:li [:strong "Attribute"] " associates an Attribute with the Entity"]
    [:li [:strong "Value"] " defines the Value of the Attribute associated with the Entity"]]

   [:p "We can model the information from the SQL database
   using these three columns from the Universal Schema,
   and 4 new Attributes."]

   (app.ui/code '[:person/first-name
                  :person/last-name
                  :likes/food
                  :likes/drink])

   [:p "With this structure, data for Helena and Noah could look like this:"]

   [:table
    [:tr [:td "Entity"] [:td "Attribute"] [:td "Value"]]
    [:tr [:td "1000"] [:td ":person/first-name"] [:td "Helena"]]
    [:tr [:td "1000"] [:td ":person/last-name"] [:td "Almeida"]]
    [:tr [:td "1000"] [:td ":likes/food"] [:td "pizza"]]
    [:tr [:td "1000"] [:td ":likes/drink"] [:td "beer"]]
    [:tr [:td ""] [:td ",,,"] [:td ""]]
    [:tr [:td "1006"] [:td ":person/first-name"] [:td "Noah"]]
    [:tr [:td "1006"] [:td ":person/last-name"] [:td "Silva"]]
    [:tr [:td "1006"] [:td ":likes/food"] [:td "curry"]]
    [:tr [:td "1006"] [:td ":likes/drink"] [:td "beer"]]]


   [:p "Let's implement it."]

   [:p "To install Attributes we need to define at least three things for each of them:"]

   [:ul
    [:li ":db/ident an identifier, like :person/first-name"]
    [:li ":db/valueType a type for the values of this attribute, like :db.type/string or "
     [:a {:href "https://docs.datomic.com/schema/schema-reference.html#db-valuetype"} "others"]]
    [:li ":db/cardinality whether this attribute accepts "
     [:a {:href "https://docs.datomic.com/schema/schema-reference.html#db-cardinality"} "one"]
     " or "
     [:a {:href "https://docs.datomic.com/schema/schema-reference.html#db-cardinality"} "many"]
     " values"]]

   [:p "Let's say that the people in our database can only like one food and one drink at the same time.
   Our attributes could be defined and installed like this:"]

   (app.ui/code '(defn install-attributes [conn]
                   @(d/transact conn [{:db/ident :person/first-name
                                       :db/valueType :db.type/string
                                       :db/cardinality :db.cardinality/one
                                       :db/doc "A person's name"
                                       :db/unique :db.unique/identity}

                                      {:db/ident :person/last-name
                                       :db/valueType :db.type/string
                                       :db/cardinality :db.cardinality/one
                                       :db/doc "A person's last name"
                                       :db/unique :db.unique/identity}

                                      {:db/ident :likes/drink
                                       :db/valueType :db.type/string
                                       :db/cardinality :db.cardinality/one
                                       :db/doc "A favorite drink"}

                                      {:db/ident :likes/food
                                       :db/valueType :db.type/string
                                       :db/cardinality :db.cardinality/one
                                       :db/doc "A favorite food"}])))

   [:p "Now that our attributes are installed, let's add our data."]

   (app.ui/code '(defn load-data [conn]
                   @(d/transact conn [{:person/first-name "Helena"
                                       :person/last-name "Almeida"
                                       :likes/food "pizza"
                                       :likes/drink "beer"}

                                      {:person/first-name "Alice"
                                       :person/last-name "Campos"
                                       :likes/food "sushi"
                                       :likes/drink "wine"}

                                      {:person/first-name "Laura"  :person/last-name "Ferreira" :likes/food "pizza" :likes/drink "water"}
                                      {:person/first-name "Miguel" :person/last-name "Melo"     :likes/food "pizza" :likes/drink "water"}
                                      {:person/first-name "Arthur" :person/last-name "Ramos"    :likes/food "tacos" :likes/drink "beer"}
                                      {:person/first-name "Noah"   :person/last-name "Silva"    :likes/food "curry" :likes/drink "beer"}])))

   [:p "Finally, we can query our Datomic database to find the names of your friends who like pizza. Click \"Run\" below."]

   (app.ui/runnable "friends"
                    '[:find ?n
                      :where [?e :likes/food "pizza"]
                      [?e :person/first-name ?n]])


   [:h2 "Practice"]

   [:p "Edit the query above to:"]
   [:ol
    [:li "Find the :person/first-name and :person/last-name of friends who like pizza"]
    [:li "Only show friends who like pizza and beer"]]

   [:h2 "Bonus Queries"]
   [:p "What everyone likes"]
   (app.ui/runnable "friends"
                    '[:find ?n ?food ?drink
                      :where
                      [?e :person/first-name ?n]
                      [?e :likes/food ?food]
                      [?e :likes/drink ?drink]])

   [:p "Everyone that has no favorite food"]
   (app.ui/runnable "friends"
                    '[:find ?fn ?ln
                      :where
                      [?e :person/first-name ?fn]
                      [?e :person/last-name ?ln]
                      [(missing? $ ?e :likes/food)]])

   [:h2 "Resources"]
   [:ul [:li [:a {:href "https://en.wikipedia.org/wiki/Entity%E2%80%93attribute%E2%80%93value_model"}
              "Entity-attribute-value model"]]
    [:li [:a {:href "https://docs.datomic.com/whatis/data-model.html#universal"} "Datomic Docs: Data Model"]]]])

(defn three []
  [:div
   [:h1 "Learn Datomic Datalog"]
   #_[:h2 "Data model"]
   [:p "Before we continue querying the database, let's take a look at our data model."]
   (app.ui/code
    '{:pokemon/name    "Bulbasaur",
      :pokemon/number  "001",
      :pokemon/type    ["Grass" "Poison"],
      :stat/attack     49,
      :stat/defense    49,
      :stat/hp         45,
      :stat/sp-attack  65,
      :stat/sp-defense 65,
      :stat/speed      45})
   [:p "The map above represents one Pokemon Entity. Each key of the map (:stat/speed, :pokemon/number, :pokemon/name, etc) represents one Attribute and each value of the map (45, \"001\", \"Bulbasaur\", etc) represents one or more Values."]
   #_[:p "It is important to understand the EAV (Entity, Attribute, Value) structure as we use it to contruct our Datalog queries."]

   [:p "Each Entity has an entity-id. Let's find \"Bulbasaur\"'s entity-id"]
   (app.ui/runnable "pokemon"
                    [:find '?entity-id
                     :where ['?entity-id :pokemon/name "Bulbasaur"]])
   [:p "Now let's list all of the Attributes associated with \"Bulbasaur\""]
   (app.ui/runnable "pokemon"
                    [:find '?attribute
                     :where
                     ['?entity-id :pokemon/name "Bulbasaur"]
                     ['?entity-id '?attribute-id '_]
                     ['?attribute-id :db/ident '?attribute]])
   [:p "Finally, let's list all of the Attributes and values associated with \"Bulbasaur\""]
   (app.ui/runnable "pokemon"
                    [:find '?attribute '?value
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
   (app.ui/runnable "pokemon"
                    '[:find ?name ?type :where
                      (not [?entity :pokemon/type "Grass"])
                      [?entity :pokemon/name ?name]
                      [?entity :pokemon/type ?type]])

   (app.ui/runnable "pokemon"
                    '[:find ?name ?speed :where
                      [?entity :pokemon/name ?name]
                      [?entity :stat/speed ?speed]
                      [(> ?speed 80)]])])

(def chapters
  (array-map
   nil             {:nav-title "Index" :content one}
   "modeling-data" {:nav-title "Modeling data" :content two}
   ;;"querying"      {:nav-title "Querying" :content three}
   ))
