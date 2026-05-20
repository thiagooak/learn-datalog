(ns app.core-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [app.core :refer [run-q]]))

(deftest run-q-unsafe-input
  (testing "shell execution"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(clojure.java.shell/sh \"ls\") ?x]]"))))

  (testing "System/exit"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(System/exit 0) ?x]]"))))

  (testing "arbitrary java interop"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(java.io.File. \"/etc/passwd\") ?x]]"))))

  (testing "eval"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(eval '(println \"pwned\")) ?x]]"))))

  (testing "slurp"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(slurp \"/etc/passwd\") ?x]]"))))

  (testing "namespace-qualified clojure fn outside allowlist"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [(clojure.core/slurp \"/etc/passwd\") ?x]]"))))

  (testing "fn special form"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "pokemon" "[:find ?x :where [((fn [] (System/exit 0))) ?x]]"))))

  (testing "safe queries are not rejected"
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name]]"))
    (is (run-q "pokemon" "[:find ?name ?speed :where [?e :pokemon/name ?name] [?e :stat/speed ?speed] [(> ?speed 80)]]"))
    (is (run-q "pokemon" "[:find ?name :where (not [?e :pokemon/type \"Grass\"]) [?e :pokemon/name ?name]]"))
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name] [(clojure.string/starts-with? ?name \"B\")]]"))
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name] [(clojure.string/includes? ?name \"saur\")]]"))
    (is (run-q "pokemon" "[:find ?name ?type :where [?e :pokemon/name ?name] [?e :pokemon/type ?type] (not-join [?e ?type] [?e :pokemon/type ?other] [(not= ?type ?other)])]"))
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name] (or [?e :pokemon/type \"Fire\"] [?e :pokemon/type \"Water\"])]"))
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name] (or-join [?e] [?e :pokemon/type \"Electric\"] (and [?e :stat/speed ?speed] [(> ?speed 100)]))]"))
    (is (run-q "pokemon" "[:find ?name :where [?e :pokemon/name ?name] [?e :pokemon/type ?type1] [?e :pokemon/type ?type2] [(not= ?type1 ?type2)]]"))
    (is (run-q "pokemon" "[:find ?name (count ?type) :where [?e :pokemon/name ?name] [?e :pokemon/type ?type]]"))
    (is (run-q "pokemon" "[:find (min ?speed) (max ?speed) :where [?e :stat/speed ?speed]]"))
    (is (run-q "pokemon" "[:find (avg ?attack) :where [?e :stat/attack ?attack]]"))
    (is (run-q "pokemon" "[:find (sum ?hp) :where [?e :stat/hp ?hp]]"))
    (is (run-q "pokemon" "[:find (count ?type) :with ?e :where [?e :pokemon/type ?type]]"))
    (is (run-q "pokemon" "[:find ?name (count-distinct ?type) :where [?e :pokemon/name ?name] [?e :pokemon/type ?type]]"))
    (is (run-q "pokemon" "[:find [?name ...] :where [?e :pokemon/name ?name] [?e :stat/speed ?speed] [(> ?speed 100)]]"))
    (is (run-q "pokemon" "[:find (max ?speed) . :where [?e :stat/speed ?speed]]"))
    (is (run-q "pokemon" "[:find [?number ?type ?speed] :where [?e :pokemon/name \"Pikachu\"] [?e :pokemon/number ?number] [?e :pokemon/type ?type] [?e :stat/speed ?speed]]"))
    (is (run-q "pokemon" "[:find (pull ?e [:pokemon/name :stat/speed]) :where [?e :stat/speed ?speed] [(> ?speed 100)]]"))
    (is (run-q "pokemon" "[:find (pull ?e [*]) :where [?e :pokemon/name \"Charizard\"]]"))
    (is (run-q "pokemon" "[:find (pull ?e [:pokemon/name :pokemon/number :pokemon/type :stat/hp :stat/speed]) . :where [?e :pokemon/name \"Pikachu\"]]"))))

(comment
  (run-tests))
