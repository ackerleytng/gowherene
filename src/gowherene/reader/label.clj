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

(defn earlier-x-large
  "Given a loc, find the loc before this loc that has a style containing `font-size: x-large`"
  [loc]
  (hselect/prev-pred
   loc
   (hselect/attr :style #(re-find #"font-size\s*:\s*x-large" %))))

(defn label
  [loc]
  (when-let [found ((some-fn
                     earlier-header
                     earlier-x-large)
                    loc)]
    (content (subtree found))))

(defn add-label
  [{:keys [loc] :as input}]
  (assoc input :label (label loc)))
