(ns utils.common.password
  (:refer-clojure :exclude [hash])
  (:require [clojure.string :as str])
  (:require [utils.common.hash :as hash])
  (:require [utils.common.parse :as parse])
  (:import [org.apache.commons.codec.binary Base64]))

;;
;; utility functions
;;

(def rnd (java.util.Random.))

(defn random-element [coll]
  (nth coll (.nextInt rnd (count coll))))

(def upper-case "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def lower-case "abcdefghijklmnopqrstuvwxyz")
(def numbers    "0123456789")
(def punctuation "!~@#%^&*()-_+={}[]|?<>.,:;")

(def characters (map char (concat lower-case numbers punctuation upper-case)))

(defn character []
  (random-element characters))

(defn random-word [length]
  (apply str (take length (repeatedly character))))

;;
;; password hashing
;;

(def digestToAlgorithm {
                        :MD5X :MD5
                        :SHA1 :SHA
                        :SHA2 :SHA-256
                        :SHA3 :SHA-384
                        :SHA5 :SHA-512
                        })

(def algorithmToDigest {
                        :MD5     :MD5X
                        :SHA     :SHA1
                        :SHA-256 :SHA2
                        :SHA-384 :SHA3
                        :SHA-512 :SHA5
                        })

(defn random-digest []
  (random-element ["md5" "sha" "sha-256" "sha-384" "sha-512"]))

(defn iterations [s]
  (inc (reduce + (map #(int %) s))))

(defn find-digest-algorithm [digest]
  (let [kw (keyword (str/upper-case digest))
        d (or (kw algorithmToDigest)
              kw)
        algorithm (or (d digestToAlgorithm)
                      (throw (IllegalArgumentException. (str "invalid digest! [" digest "]"))))]
    [(name d) (name algorithm)]))

(defn hash [digest pw]
  (let [[digest algorithm] (find-digest-algorithm digest)
        salt-size (+ 15 (.nextInt rnd 10))
        salt (random-word salt-size)]
    (String. (Base64/encodeBase64 (.getBytes (str digest "$" salt-size "$" salt "$" (hash/hash algorithm (iterations salt) salt pw)))))))

(def digest-take 4)
(def salt-size-drop 5)
(def salt-size-take 2)
(def salt-drop 8)

(defn str-take [n s]
  (apply str (take n s)))

(defn str-drop [n s]
  (apply str (drop n s)))

(defn matches? [pw check]
  (if-let [s (String. (Base64/decodeBase64 (.getBytes check)))]
    (let [digest (str-take digest-take s)
          size   (parse/parse-number (str-take salt-size-take (str-drop salt-size-drop s)))
          salt   (str-take size (str-drop salt-drop s))
          hash (str-drop (inc (+ salt-drop size)) s)]
      (= (hash/hash (name ((keyword digest) digestToAlgorithm)) (iterations salt) salt pw) hash))))


;;
;; password strength
;;

(def min-length 7)

(defn contains-char? [c s]
  (some #(= c %) s))

(defn has-lower-case? [s]
  (> (count (filter #(contains-char? % s) lower-case)) 0))

(defn has-upper-case? [s]
  (> (count (filter #(contains-char? % s) upper-case)) 0))

(defn has-punctuation? [s]
  (> (count (filter #(contains-char? % s) punctuation)) 0))

(defn has-number? [s]
  (> (count (filter #(contains-char? % s) numbers)) 0))

(defn strong? [s]
  (and (> (count s) min-length)
       (has-number? s)
       (has-punctuation? s)
       (has-lower-case? s)
       (has-upper-case? s)))

