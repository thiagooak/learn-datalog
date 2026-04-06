(ns app.core-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [app.core :refer [run-q]]))

(deftest run-q-unsafe-input
  (testing "shell execution"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(clojure.java.shell/sh \"ls\") ?x]]"))))

  (testing "System/exit"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(System/exit 0) ?x]]"))))

  (testing "arbitrary java interop"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(java.io.File. \"/etc/passwd\") ?x]]"))))

  (testing "eval"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(eval '(println \"pwned\")) ?x]]"))))

  (testing "slurp"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(slurp \"/etc/passwd\") ?x]]"))))

  (testing "namespace-qualified clojure fn outside allowlist"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [(clojure.core/slurp \"/etc/passwd\") ?x]]"))))

  (testing "fn special form"
    (is (thrown-with-msg? Exception #"Unsafe Query"
                          (run-q "[:find ?x :where [((fn [] (System/exit 0))) ?x]]"))))

  (testing "safe queries are not rejected"
    (is (run-q "[:find ?name :where [?e :pokemon/name ?name]]"))
    (is (run-q "[:find ?name ?speed :where [?e :pokemon/name ?name] [?e :stat/speed ?speed] [(> ?speed 80)]]"))
    (is (run-q "[:find ?name :where (not [?e :pokemon/type \"Grass\"]) [?e :pokemon/name ?name]]"))))

(comment
  (run-tests))
