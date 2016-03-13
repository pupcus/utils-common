(ns utils.common.html
  (:require [clojure.string :as str]
            [hiccup.core :refer :all]))

(defn- encode [s]
  (str/replace s #"[^a-zA-Z0-9]" "_"))

(defn hidden-input [k v]
  [:input {:type "hidden" :name (name k) :value (str v)}])

(defn hidden-params [params]
  (html
   (doall
    (for [[k v] (apply hash-map params)]
      (hidden-input k v)))))

(defn post-link [id label action & params]
  (let [form-id (format "form_%s_%s" (encode label) (str id))]
    (html
     [:div {:style "display: none"}
      [:form {:id form-id :method "POST" :action action}
       [:input {:type "hidden" :name "id" :value id}]
       (hidden-params params)]
      [:script {:type "text/javascript"}
       (apply format "function submit_%s() { document.forms['%s'].submit(); }" (repeat 2 form-id))]]
     [:span [:a {:href (format "javascript: submit_%s()" form-id)} label]])))
