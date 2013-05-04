(ns utils.common.errors)

(def ^:dynamic *errors*)

(defn set-error [key message]
  (if (bound? #'*errors*)
    (swap! *errors* conj [key message])))

(defn has-errors? []
  (if (bound? #'*errors*)
    (> (count (deref *errors*)) 0)
    false))

(defn has-error? [key]
  (if (bound? #'*errors*)
    (if-let [msg ((deref *errors*) key)]
      true)))

(defn no-errors? []
  (not (has-errors?)))

(defn error-msg [key]
  (if (bound? #'*errors*)
    ((deref *errors*) key)))

(defn error-msgs []
  (if (bound? #'*errors*)
    (deref *errors*)))

(defmacro with-errors [& body]
  `(binding [~'utils.common.errors/*errors* (atom {})]
     ~@body))
