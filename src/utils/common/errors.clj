(ns utils.common.errors)

(def ^:dynamic *errors*)

(defn set-error [key message]
  (when (bound? #'utils.common.errors/*errors*)
    (let [messages (get (deref utils.common.errors/*errors*) key [])]
      (swap! *errors* assoc key (conj messages message)))))

(defn no-errors? []
  (if (bound? #'utils.common.errors/*errors*)
    (empty? (deref utils.common.errors/*errors*))
    true))

(defn has-errors? []
  (not (no-errors?)))

(defn error-msgs []
  (when (bound? #'utils.common.errors/*errors*)
    (deref utils.common.errors/*errors*)))

(defn error-msg [key]
  (when (bound? #'utils.common.errors/*errors*)
    ((deref utils.common.errors/*errors*) key)))

(defn has-error? [key]
  (error-msg key))

(defmacro with-errors [& body]
  `(binding [~'utils.common.errors/*errors* (atom {})]
     ~@body))

(defn wrap-errors [handler]
  (fn [req]
    (with-errors
      (handler req))))
