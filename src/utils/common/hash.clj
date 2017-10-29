(ns utils.common.hash
  (:refer-clojure :exclude [hash bytes]))

(defn- bytes [^String s]
  (if (nil? s)
    (byte-array s)
    (.getBytes s)))

(defn- hash* [md text salt]
  (let [target (str text salt)]
    (.update md (bytes target))
    (String. (org.apache.commons.codec.binary.Hex/encodeHex (.digest md)))))

(defn hash [digest iterations salt text]
  (let [md (java.security.MessageDigest/getInstance digest)
        t (String. (bytes text))
        s  (String. (bytes salt))]
    (loop [count (dec iterations)
           result (hash* md t s)]
      (if (> count 0)
        (recur (dec count) (hash* md t result))
        result))))

