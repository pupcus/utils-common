(ns utils.common.errors.test
  (:use clojure.test)
  (:require [utils.common.errors :as errors]))

(deftest unbound-test
    (is (true? (errors/no-errors?)))
    (is (false? (errors/has-errors?)))
    (is (nil? (errors/has-error? :key)))
    (is (nil? (errors/error-msg :key)))
    (is (nil? (errors/error-msgs))))

(deftest no-errors-test
  (errors/with-errors
    (is (true? (errors/no-errors?)))
    (is (false? (errors/has-errors?)))))

(deftest error-test
  (errors/with-errors
    (errors/set-error :key "a message")
    (is (false? (errors/no-errors?)))
    (is (true? (errors/has-errors?)))
    (is (true? (errors/has-error? :key)))
    (is (= "a message" (errors/error-msg :key)))
    (is (= {:key "a message"} (errors/error-msgs)))))
