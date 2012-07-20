(ns utils.common.properties
  (:refer-clojure :exclude [load]))

(defn load [file]
  (let [p (doto (java.util.Properties.) (.load (java.io.FileInputStream. (java.io.File. file))))]
    (reduce conj {} (map (fn [x] [(keyword (.getKey x)) (.getValue x)]) (seq p)))))

