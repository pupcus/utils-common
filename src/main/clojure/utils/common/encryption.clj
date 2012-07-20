(ns utils.common.encryption
  (:import (javax.crypto.spec DESedeKeySpec))
  (:import (javax.crypto SecretKey SecretKeyFactory Cipher))
  (:import (org.apache.commons.codec.binary Base64))
  (:require [utils.common.properties :as properties]))

(def encryption-properties (properties/load (str (System/getProperty "user.home") "/.properties.d/encryption")))

(def create-cipher
  (memoize (fn [mode]
             (let [algorithm "DESede"
                   keySpec (DESedeKeySpec. (.getBytes (:encryption.key encryption-properties)))
                   keyFactory (SecretKeyFactory/getInstance algorithm)
                   key (.generateSecret keyFactory keySpec)]
               (doto (Cipher/getInstance algorithm)
                 (.init mode key))))))

(defn protect [text]
  (->> (.getBytes text)
       (.doFinal (create-cipher Cipher/ENCRYPT_MODE))
       (Base64/encodeBase64)
       (String.)))

(defn unprotect [text]
  (->> (.getBytes text)
       (Base64/decodeBase64)
       (.doFinal (create-cipher Cipher/DECRYPT_MODE))
       (String.)))
