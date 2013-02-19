(ns utils.common.properties
  (:refer-clojure :exclude [load]))

(defmulti load class)

(defmethod load java.io.InputStream [is]
  (let [p (doto (java.util.Properties.) (.load is))]
    (reduce conj {} (map (fn [x] [(keyword (.getKey x)) (.getValue x)]) (seq p)))))

(defmethod load java.net.URL [resource]
  (with-open [is (.openStream resource)]
    (load is)))

(defmethod load java.io.File [file]
  (with-open [is (java.io.FileInputStream. file)]
    (load is)))

(defmethod load java.lang.String [filename]
  (let [file (java.io.File. filename)]
    (load file)))

