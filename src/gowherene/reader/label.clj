(ns gowherene.reader.label
  (:require [clojure.zip :as zip]
            [hickory.select :as hselect]
            [gowherene.reader.utils :refer [content subtree]]))

(defn earlier-header
  "Given a loc, find the header just above or before this loc."
  [loc]
  (hselect/prev-pred
   loc
   (apply hselect/or (map hselect/tag [:h1 :h2 :h3 :h4]))))

(defn content-earlier-header
  [loc]
  (let [header-loc (earlier-header loc)]
    (when header-loc
      (content (subtree header-loc)))))

(defn add-label
  [{:keys [loc] :as input}]
  (assoc input :label (content-earlier-header loc)))
