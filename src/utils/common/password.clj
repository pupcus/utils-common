(ns utils.common.password
  (:refer-clojure :exclude [hash])
  (:require [clojure.string :as str]
            [utils.common.hash :as hash]))

(def upper-case "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def lower-case "abcdefghijklmnopqrstuvwxyz")
(def numbers    "0123456789")
(def punctuation "!~@#%^&*()-_+={}[]|?<>.,:;")

(def characters (map char (concat lower-case numbers punctuation upper-case)))

(defn character []
  (rand-nth characters))

(defn random-word [length]
  (apply str (take length (repeatedly character))))

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
  (rand-nth ["md5" "sha" "sha-256" "sha-384" "sha-512"]))

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
        salt-size (+ 15 (rand-int 10))
        salt (random-word salt-size)]
    (String.
     (org.apache.commons.codec.binary.Base64/encodeBase64
      (.getBytes (str digest "$" salt-size "$" salt "$" (hash/hash algorithm (iterations salt) salt pw)))))))

(def digest-take 4)
(def salt-size-drop 5)
(def salt-size-take 2)
(def salt-drop 8)

(defn str-take [n s]
  (apply str (take n s)))

(defn str-drop [n s]
  (apply str (drop n s)))

(defn matches? [pw check]
  (if-let [s (String. (org.apache.commons.codec.binary.Base64/decodeBase64 (.getBytes check)))]
    (let [digest (str-take digest-take s)
          size   (read-string (str-take salt-size-take (str-drop salt-size-drop s)))
          salt   (str-take size (str-drop salt-drop s))
          hash (str-drop (inc (+ salt-drop size)) s)]
      (= (hash/hash (name ((keyword digest) digestToAlgorithm)) (iterations salt) salt pw) hash))))


;;
;; password strength
;;

(def default-options
  {:length 8
   :number-nummeric 2
   :number-punctuation 1
   :number-lower-case 1
   :number-upper-case 1})

(defn contains-char? [c s]
  (some #(= c %) s))

(defn has-lower-case? [s number-lower-case]
  (>= (count (filter #(contains-char? % s) lower-case)) number-lower-case))

(defn has-upper-case? [s number-upper-case]
  (>= (count (filter #(contains-char? % s) upper-case)) number-upper-case))

(defn has-punctuation? [s number-punctuation]
  (>= (count (filter #(contains-char? % s) punctuation)) number-punctuation))

(defn has-number? [s number-numeric]
  (>= (count (filter #(contains-char? % s) numbers)) number-numeric))

(defn strong?
  ([s]
   (strong? s default-options))
  ([s options]
   (let [{:keys [length number-numeric number-punctuation number-lower-case number-upper-case]} (merge default-options options)]
     (and (>= (count s) length)
          (has-number? s number-numeric)
          (has-punctuation? s number-punctuation)
          (has-lower-case? s number-lower-case)
          (has-upper-case? s number-upper-case)))))

