(ns utils.common.image
  (:refer-clojure :exclude [read])
  (:require [clojure.tools.logging :as log])
  (:import (java.io File ByteArrayInputStream ByteArrayOutputStream InputStream BufferedInputStream FileInputStream))
  (:import (java.awt Graphics2D RenderingHints Transparency))
  (:import (java.awt.image BufferedImage))
  (:import (javax.imageio ImageIO ImageReader ImageWriter))
  (:import (javax.imageio.stream ImageInputStream ImageOutputStream))
  (:require [utils.common.file :as file-utils]))


(defmacro assert-args [fnname & pairs]
  `(do (when-not ~(first pairs)
         (throw (IllegalArgumentException.
                  ~(str fnname " requires " (second pairs)))))
     ~(let [more (nnext pairs)]
        (when more
          (list* `assert-args fnname more)))))


(defmacro with-disposal [bindings & body]
  (assert-args with-disposal
     (vector? bindings) "a vector for its binding"
     (even? (count bindings)) "an even number of forms in binding vector")
  (cond
    (= (count bindings) 0) `(do ~@body)
    (symbol? (bindings 0)) `(let ~(subvec bindings 0 2)
                              (try
                                (with-disposal ~(subvec bindings 2) ~@body)
                                (finally
                                  (. ~(bindings 0) dispose))))
    :else (throw (IllegalArgumentException.
                   "with-dispose only allows Symbols in bindings"))))

(def image-suffix "png")

(defn image-type [image]
  (if (= (.getTransparency image) (Transparency/OPAQUE))
    (BufferedImage/TYPE_INT_RGB)
    (BufferedImage/TYPE_INT_ARGB)))

(defn- resize* [image w h type]
  (let [ret (BufferedImage. w h type)]
    (doto (.createGraphics ret)
      (.setRenderingHint RenderingHints/KEY_INTERPOLATION RenderingHints/VALUE_INTERPOLATION_BICUBIC)
      (.setRenderingHint RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY)
      (.drawImage image 0 0 w h nil)
      (.dispose))
    ret))

(defn step-down-by-width [w h width aspect]
  (let [tw (/ w 2)
        nw (if (> tw width) tw width)
        nh (int (* nw (/ 1.0 aspect)))]
    [nw nh]))

(defn resize-to-width [image width]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        aspect (/ w (* 1.0 h))]
    (loop [ret image
           [nw nh] (step-down-by-width w h width aspect)]
      (if (> nw width)
        (recur (resize* ret nw nh type) (step-down-by-width nw nh width aspect))
        (resize* ret nw nh type)))))

(defn step-down-by-height [w h height aspect]
  (let [th (/ h 2)
        nh (if (> th height) th height)
        nw (int (* nh aspect))]
    [nw nh]))

(defn resize-to-height [image height]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        aspect (/ w (* 1.0 h))]
    (loop [ret image
           [nw nh] (step-down-by-height w h height aspect)]
      (if (> nh height)
        (recur (resize* ret nw nh type) (step-down-by-height nw nh height aspect))
        (resize* ret nw nh type)))))

(defn crop-horizontal [image max-width]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        offset (/ (- w max-width) 2)
        ret (BufferedImage. max-width h type)]
    (doseq [i (range max-width), j (range h)] (.setRGB ret i j (.getRGB image (+ i offset) j)))
    ret))

(defn crop-vertical [image max-height]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        offset (/ (- h max-height) 2)
        ret (BufferedImage. w max-height type)]
    (doseq [i (range w), j (range max-height)] (.setRGB ret i j (.getRGB image i (+ j offset))))
    ret))

(defn rotate-counter-clockwise [image]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        ret (BufferedImage. h w type)]
    (doseq [i (range w), j (range h)] (.setRGB ret j (- w 1 i) (.getRGB image i j)))
    ret))

(defn rotate-clockwise [image]
  (let [w (.getWidth image)
        h (.getHeight image)
        type (image-type image)
        ret (BufferedImage. h w type)]
    (doseq [i (range w), j (range h)] (.setRGB ret (- h 1 j) i (.getRGB image i j)))
    ret))

(defn normalize-image [image aspect target-aspect]
  (cond
   (and (> target-aspect 1.0) (< aspect 1.0)) (rotate-counter-clockwise image)
   (and (< target-aspect 1.0) (> aspect 1.0)) (rotate-clockwise image)
   :else image))

(defn normalized-resize-to-height [image max-width max-height]
  (let [ret (resize-to-height image max-height)]
    (if (> (.getWidth ret) max-width)
      (crop-horizontal ret max-width)
      ret)))

(defn normalized-resize-to-width [image max-width max-height]
  (let [ret (resize-to-width image max-width)]
    (if (> (.getHeight ret) max-height)
      (crop-vertical ret max-height)
      ret)))

(defn resize [image max-width max-height]
  (let [w (.getWidth image)
        h (.getHeight image)
        aspect (/ w (* 1.0 h))
        target-aspect (/ max-width (* 1.0 max-height))
        nimg (normalize-image image aspect target-aspect)]
    (if (> aspect 1.5)
      (normalized-resize-to-height nimg max-width max-height)
      (normalized-resize-to-width nimg max-width max-height))))

(defn image-writer [suffix]
  (try
    (let [writer (.next (ImageIO/getImageWritersBySuffix suffix))]
      (log/debug (str "using first writer: " (.getVendorName (.getOriginatingProvider writer))))
      writer)
    (catch Exception e
      (log/debug (str "unable to find image writer for suffix [" suffix "]")))))

(defn image-reader [suffix]
  (try
    (let [reader (.next (ImageIO/getImageReadersBySuffix suffix))]
      (log/debug (str "using first reader: " (.getVendorName (.getOriginatingProvider reader))))
      reader)
    (catch Exception e
      (log/debug (str "unable to find image reader for suffix [" suffix "]")))))

(defn read [filename]
  (try
    (let [file (java.io.File. filename)]
      (with-disposal [reader (image-reader (file-utils/extension (.toString file)))]
        (with-open [in (ImageIO/createImageInputStream (BufferedInputStream. (FileInputStream. file)))]
          (.setInput reader in false)
          (.read reader 0))))
    (catch Exception e
      (log/debug (str "unable to read image from file [" filename "]"))
      (.printStackTrace e))))

(defn save [image file]
  (try
    (let [extension (file-utils/extension file)]
      (with-disposal [writer (image-writer extension)]
        (with-open [os (ImageIO/createImageOutputStream file)]
          (.setOutput writer os)
          (ImageIO/write image extension os))))
    (catch Exception e
      (log/debug (str "unable to save image file [" file "]"))
      (.printStackTrace e))))

(defn to-ITextImage [image]
  (try
    (with-disposal [writer (image-writer image-suffix)]
      (let [output (ByteArrayOutputStream.)]
        (with-open [os (ImageIO/createImageOutputStream output)]
          (.setOutput writer os)
          (ImageIO/write image image-suffix os)
          (com.lowagie.text.Image/getInstance (.toByteArray output)))))
    (catch Exception e
      (log/debug "unable to convert BufferedImage to iText image format!"))))

(defn to-BufferedImage [data]
  (try
    (ImageIO/read (ByteArrayInputStream. data))
    (catch Exception e
      (log/debug (str "unable to decode image data to BufferedImage: " (.getMessage e)))
      (.printStackTrace e))))

