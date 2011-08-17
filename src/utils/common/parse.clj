(ns utils.common.parse)

(defmulti parse-int type)
(defmethod parse-int java.lang.Integer [n] n)
(defmethod parse-int java.lang.String [s]
  (try
    (Integer/parseInt s)
    (catch NumberFormatException e 0)))
(defmethod parse-int :default [x] 0)

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

