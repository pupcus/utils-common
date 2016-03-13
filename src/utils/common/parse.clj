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
(defmethod parse-number java.lang.Byte          [n] n)
(defmethod parse-number java.lang.Short         [n] n)
(defmethod parse-number java.lang.Integer       [n] n)
(defmethod parse-number java.lang.Long          [n] n)
(defmethod parse-number java.math.BigInteger    [n] n)
(defmethod parse-number java.lang.Float         [n] n)
(defmethod parse-number java.lang.Double        [n] n)
(defmethod parse-number java.math.BigDecimal    [n] n)

(defmethod parse-number java.lang.String  [s]
  (try
    (let [n (java.math.BigDecimal. s)]
      (if (= (.remainder n 1M) 0M)
        (fit-result n)
        n))
    (catch Exception e 0)))

(defmethod parse-number :default [x] 0)

(defn parse-date* [s format]
  (try
    (let [sdf (java.text.SimpleDateFormat. format)]
      (.parse sdf s))
    (catch java.text.ParseException e)))

(defn parse-date [s formats]
  (if (> (count formats) 0)
    (let [format (first formats)]
      (if-let [date (parse-date* s format)]
        date
        (recur s (rest formats))))))

