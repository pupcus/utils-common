(ns utils.common.database)

(defn get-timestamp-string []
  (let [sdf (java.text.SimpleDateFormat. "yyyy-MM-dd hh:mm:ss")
        date (java.util.Date.)]
    (.setTime date (System/currentTimeMillis))
    (.format sdf date)))

(defn set-date-created [rec timestamp]
  (if-not (:id rec)
    (assoc rec :date_created timestamp)
    (dissoc rec :date_created)))

(defn set-date-updated [rec timestamp]
  (assoc rec :date_updated timestamp))

(defn timestamp-record [rec]
  (let [timestamp (get-timestamp-string)]
    (-> rec
        (set-date-created timestamp)
        (set-date-updated timestamp))))

(defn map-keyword-to-string [rec kw]
  (if (kw rec)
    (assoc rec kw (name (kw rec)))
    rec))

(defn map-string-to-keyword [rec kw]
  (if (kw rec)
    (assoc rec kw (keyword (kw rec)))
    rec))
