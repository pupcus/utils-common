(ns utils.common.encryption
  (:import (javax.crypto.spec DESedeKeySpec))
  (:import (javax.crypto SecretKey SecretKeyFactory Cipher))
  (:import (org.apache.commons.codec.binary Base64)))

(def *encryption-key* "comGoogleGeolocationKey_i2mdpk!")

(def create-cipher
  (memoize (fn [mode]
             (let [algorithm "DESede"
                   keySpec (DESedeKeySpec. (.getBytes *encryption-key*))
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
