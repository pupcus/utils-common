(ns utils.common.test.encryption
  (:use clojure.test)
  (:require [utils.common.encryption :as encryption]))

(deftest test-protection
  (let [text "abcDefgHijkLmnoPqrsT"
        result "zTtPpuPsquIX2CQQ0bvFKmx2YbRc5R82"]
    (is (= result (encryption/protect text)) "encryption not working")))

(deftest test-unprotection
  (let [text "zTtPpuPsquIX2CQQ0bvFKmx2YbRc5R82"
        result "abcDefgHijkLmnoPqrsT"]
    (is (= result (encryption/unprotect text)) "encryption not working")))
