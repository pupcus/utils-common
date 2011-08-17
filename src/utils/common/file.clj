(ns utils.common.file)

(def *extension-separator* ".")
(def *path-separator* (System/getProperty "file.separator"))

(defmulti extension class)

(defmethod extension java.lang.String [file]
  (let [pos (.lastIndexOf file *extension-separator*)]
    (if (> pos 0)
      (.substring file (inc pos))
      (str ""))))

(defmethod extension java.io.File [file]
  (let [fullpath (.toString file)]
    (extension fullpath)))

(defmulti filename class)

(defmethod filename java.lang.String [file]
  (let [dot (.lastIndexOf file *extension-separator*)
        sep (.lastIndexOf file *path-separator*)
        start (if (> sep 0) (inc sep) 0)
        stop (if (> dot 0) dot (count file))]
    (.substring file start stop)))

(defmethod filename java.io.File [file]
  (let [fullpath (.toString file)]
    (filename fullpath)))

(defmulti path class)

(defmethod path java.lang.String [file]
  (let [sep (.lastIndexOf file *path-separator*)]
    (if (> sep 0)
      (.substring file 0 (inc sep))
      (str ""))))

(defmethod path java.io.File [file]
  (let [fullpath (str (.toString file) (if (.isDirectory file) "/"))]
    (path fullpath)))
