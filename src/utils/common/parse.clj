(ns utils.common.parse)

(defn- can-be-long? [n]
  (try
    (.longValueExact n)
    (catch ArithmeticException e nil)))

(defn- fit-result [n]
  (cond
   (can-be-long? n)  (.longValue n)
   :otherwise        (.toBigInteger n)))

(defmulti parse-number type)

(defmethod parse-number java.lang.Number [n] n)

(defmethod parse-number java.lang.String  [s]
  (try
    (let [n (java.math.BigDecimal. s)]
      (if (= (.remainder n 1M) 0M)
        (fit-result n)
        n))
    (catch Exception e 0)))

(defmethod parse-number :default [x] 0)
