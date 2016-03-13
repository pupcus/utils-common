(ns utils.common.properties
  (:refer-clojure :exclude [load])
  (require [clojure.tools.logging :as log]))

(defn- available? [^java.io.File file]
  (and (.exists file) (.canRead file)))

(defn- open-stream [resource]
  (try
    (.openStream resource)
    (catch Exception e
      (log/debug (format "unable to open stream for resource [%s]" resource)))))

(defmulti load class)

(defmethod load java.io.InputStream [is]
  (let [p (doto (java.util.Properties.) (.load is))]
    (reduce conj {} (map (fn [x] [(keyword (.getKey x)) (.getValue x)]) (seq p)))))

(defmethod load java.net.URL [resource]
  (with-open [is (open-stream resource)]
    (if (not (nil? is))
      (load is)
      {})))

(defmethod load java.io.File [file]
  (if (available? file)
    (with-open [is (java.io.FileInputStream. file)]
      (load is))
    (do
      (log/debug (format "file [%s] does not exist or unable to read" file))
      {})))

(defmethod load java.lang.String [filename]
  (let [file (java.io.File. filename)]
    (load file)))

