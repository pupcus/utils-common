(ns utils.common.encryption)

(def create-cipher
  (memoize (fn [mode keyphrase]
             (let [algorithm "DESede"
                   keySpec (javax.crypto.spec.DESedeKeySpec. (.getBytes ^String keyphrase))
                   keyFactory (javax.crypto.SecretKeyFactory/getInstance algorithm)
                   key (.generateSecret keyFactory keySpec)]
               (doto (javax.crypto.Cipher/getInstance algorithm)
                 (.init mode key))))))

(defn protect [text keyphrase] {:pre [(string? keyphrase) (>= (count keyphrase) 24)]}
  (->> (.getBytes ^String text)
       (.doFinal (create-cipher javax.crypto.Cipher/ENCRYPT_MODE keyphrase))
       (org.apache.commons.codec.binary.Base64/encodeBase64)
       (String.)))

(defn unprotect [text keyphrase] {:pre [(string? keyphrase) (>= (count keyphrase) 24)]}
  (->> (.getBytes ^String text)
       (org.apache.commons.codec.binary.Base64/decodeBase64)
       (.doFinal (create-cipher javax.crypto.Cipher/DECRYPT_MODE keyphrase))
       (String.)))
